# -*- coding: utf-8 -*-
import os, subprocess, sys, zipfile, shutil, hashlib

# --- Machine paths come from environment variables ONLY.
# --- No local paths are baked into this repo; anyone who clones
# --- must set these before building. Examples:
# ---   set PICKTOOL_JDK=C:\path\to\jdk-21
# ---   set ANDROID_HOME=C:\path\to\android-sdk
JDK    = os.environ.get("PICKTOOL_JDK")
SDK    = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
if not JDK or not SDK:
    print("Missing required environment variables for building:")
    if not JDK: print("  PICKTOOL_JDK  -> path to JDK (e.g. C:\\jdk-21)")
    if not SDK: print("  ANDROID_HOME  -> path to Android SDK (e.g. C:\\android-sdk)")
    sys.exit(2)

ROOT   = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
BT     = os.path.join(SDK, "build-tools", "36.0.0")
ANDJAR = os.path.join(SDK, "platforms", "android-35", "android.jar")
SRC    = os.path.join(ROOT, "app", "src", "main", "java", "cn", "pickup", "launcher")
RES    = os.path.join(ROOT, "app", "src", "main", "res")
MANI   = os.path.join(ROOT, "app", "src", "main", "AndroidManifest.xml")
OUT    = os.path.join(ROOT, "app", "build", "manual")
GEN    = os.path.join(OUT, "gen")
JAC    = os.path.join(OUT, "javac")
APK0   = os.path.join(OUT, "app-unsigned.apk")
APK1   = os.path.join(OUT, "app-with-dex.apk")
APK2   = os.path.join(OUT, "app-aligned.apk")
KS     = os.path.join(os.environ["USERPROFILE"], ".android", "debug.keystore")
OUTAPK = os.path.join(ROOT, "app", "build", "outputs", "apk", "debug", "app-debug-phone.apk")

if os.path.isdir(OUT): shutil.rmtree(OUT)
os.makedirs(GEN); os.makedirs(JAC)

env = os.environ.copy()
env["JAVA_HOME"] = JDK
env["PATH"] = os.path.join(JDK, "bin") + ";" + BT + ";" + env.get("PATH", "")

def sh(cmd):
    r = subprocess.run(cmd, env=env, capture_output=True)
    if r.returncode != 0:
        print("FAILED:", " ".join(cmd))
        print((r.stdout or b"").decode("utf-8","replace")[:1500])
        print((r.stderr or b"").decode("utf-8","replace")[:1500])
        sys.exit(r.returncode)

xmls = [os.path.join(r,f) for r,_,fs in os.walk(RES) for f in fs if f.endswith(".xml")]
print("[1] aapt2 compile", len(xmls), "xml")
for x in xmls:
    subprocess.run([os.path.join(BT,"aapt2.exe"),"compile","-o",GEN,x], env=env, check=True)

# PNG drawables need aapt2 compile too (becomes .flat with bin extension)
pngs = []
for d in ["drawable", "drawable-nodpi"]:
    dd = os.path.join(RES, d)
    if not os.path.isdir(dd):
        continue
    pngs += [os.path.join(dd, f) for f in os.listdir(dd)
             if f.lower().endswith(".png")]
print("[1b] aapt2 compile png", len(pngs))
for p in pngs:
    subprocess.run([os.path.join(BT,"aapt2.exe"),"compile","-o",GEN,p], env=env, check=True)

mani_p = os.path.join(OUT, "AndroidManifest.patched.xml")
src = open(MANI, encoding="utf-8").read()
if "package=" not in src.split("\n",1)[0]:
    src = src.replace('<manifest xmlns:android="http://schemas.android.com/apk/res/android">',
        '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="cn.pickup.launcher">',1)
open(mani_p,"w",encoding="utf-8").write(src)

# PNG drawables: aapt2 compile already turned them into .flat files
# in GEN (drawable-nodpi_*.png.flat) — those are in `flats` below, so
# we must NOT append the raw .png paths to link (link wants .flat only).
png_paths = []

flats = [os.path.join(GEN,f) for f in os.listdir(GEN) if f.endswith(".flat")]
print("[2] aapt2 link", len(flats), "flats")
sh([os.path.join(BT,"aapt2.exe"),"link","-I",ANDJAR,"--manifest",mani_p,"--java",GEN,
    "-o",APK0,"--auto-add-overlay","--min-sdk-version","23","--target-sdk-version","35",
    "--version-code","8","--version-name","1.0.1"]+flats+png_paths)

srcs = ([os.path.join(SRC,f) for f in os.listdir(SRC) if f.endswith(".java")] +
        [os.path.join(GEN,f) for f in os.listdir(GEN) if f.endswith(".java")])
print("[3] javac", len(srcs))
sh([os.path.join(JDK,"bin","javac.exe"),"-d",JAC,"-encoding","UTF-8",
    "-cp",ANDJAR+";"+GEN,"-source","11","-target","11",
    "-Xlint:none","-Xlint:-options","-nowarn"]+srcs)

classes = [os.path.join(r,f) for r,_,fs in os.walk(JAC) for f in fs if f.endswith(".class")]
print("[4] d8", len(classes))
sh([os.path.join(BT,"d8.bat"),"--lib",ANDJAR,"--output",OUT,"--min-api","23"]+classes)

print("[5] inject dex")
shutil.copyfile(APK0, APK1)
with zipfile.ZipFile(APK1,"a") as z:
    z.writestr("classes.dex", open(os.path.join(OUT,"classes.dex"),"rb").read(), zipfile.ZIP_STORED)

# [5.5] PNG drawables are already packed by aapt2 link (as raw
# resources with .flat contents); no extra injection needed.
print("[5.5] PNG drawables (already in APK via aapt2 link)")

print("[6] zipalign")
sh([os.path.join(BT,"zipalign.exe"),"-p","-f","4",APK1,APK2])

print("[7] apksigner")
os.makedirs(os.path.dirname(OUTAPK), exist_ok=True)
sh([os.path.join(BT,"apksigner.bat"),"sign","--ks",KS,
    "--ks-pass","pass:android","--key-pass","pass:android","--out",OUTAPK,APK2])

sha = hashlib.sha1(open(OUTAPK,"rb").read()).hexdigest()
print("BUILT", os.path.getsize(OUTAPK), sha)
