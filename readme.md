# OC DISPLAY WEN

![homescreen](https://github.com/akhil1999/ocdisplaywen/blob/main/docs/homescreen.jpg)

Why is it called so? Well, it's an inside joke referring to what people ask ETA alot as in "VoLTE support wen" usually on forums. This is us turning the joke on users hehe.

### Why choose Kotlin? 

Well, recently I went thru Kotlin after developing some time in Java for Android, this project seemed like a good place to practically implement my Kotlin learnings.

## What does this app do?

This app is for testing the AMOLED panel refresh rate limitations.
Currently for ea8076 display panel found on Exynos7885 / 7904 / 9611 Samsung devices circa 2018.

In my experience, ea8076 panel in my Samsung Galaxy M30 (SM-M305F) can clock upto 82Hz stable but there is definitely some color shift.

##Usage

First step is to backup boot image currently on device using "BACKUP STOCK BOOTIMAGE" button. This will backup the bootimage.

Second step is to unpack the DTS which is a part of the boot image using the "UNPACK DTS" button.

Third step is to choose your custom P, M, S values which determine PLL Frequency & Refresh Rate using "MODIFY DTS" button.
This will set the chosen P, M, S values in DTS, repack the DTS & then generate a custom boot image which is flashed to boot partition by app.

Click reboot post this to test the changes, cycle the panel on & off (lock the phone and unlock it) once to make sure the change is in effect.

## How does it work

Find out [here](https://github.com/akhil1999/ocdisplaywen/blob/main/how_it_works.md)

## Dependencies / Prebuilt Binaries used:
- [magiskboot](https://github.com/topjohnwu/Magisk)
- [dtbhtool for exynos](https://github.com/akhil1999/dtc-aosp/blob/standalone/dtbtool.c)
- [dtc](https://github.com/akhil1999/dtc-aosp)

A separate repo detailing how static binaries above are built using NDK will be added shortly with steps.

## Credits:
 * [VDavid003](https://github.com/VDavid003) for haccing with me (yes, that's not a typo, it's an inside joke as well)
 * [libxzr](https://github.com/libxzr) who is best known for OSS KonaBess from which this app heavily is inspired by, thank you for your work! Would not have been possible without me reading and understanding how KonaBess works! I analysed the App which is written in Java and ported code to Kotlin as an exercise.
