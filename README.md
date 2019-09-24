# FingerSignView


## Introduction
*FingerSignView* is a simple library that lets you finger, or draw lines, smoothly with your finger into a View and save it as a file or bitmap

![Screenshot](https://raw.githubusercontent.com/AgnaldoNP/FingerSignView/master/screenshot/screenshot.png)

## Install

**Step 1**. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
**Step 2.** Add the dependency
```
dependencies {
  implementation 'com.github.AgnaldoNP:FingerSignView:1.0'
}
```
[![](https://jitpack.io/v/AgnaldoNP/FingerSignView.svg)](https://jitpack.io/#AgnaldoNP/FingerSignView)


## Usage

Sample of usage
```xml
<pereira.agnaldo.fingersignlibrary.FingerSignView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:lineColor="#344374"
            app:strokeWidth="4dp"
            app:velocityFilterWeight="4"
    />
```
### Options
| Property             | Values                           | Default |
|----------------------|----------------------------------|---------|
| lineColor            | color value                      | #000    |
| strokeWidth          | dimension value                  | 4dp     |
| velocityFilterWeight | integer value                    | 4       |


## Contributions and Support

This project was based on *[DrawingView Project](https://github.com/roscrazy/DrawingView)*

Contributions are welcome. Create a new pull request in order to submit your fixes and they shall be merged after moderation. In case of any issues, bugs or any suggestions, either create a new issue or post comments in already active relevant issues
