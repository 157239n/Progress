# Progress

This is a class to control progresses.

## Technical documentation

You can find the complete javadoc [here](http://157239n.com/page/pages/javadoc/progress/index.html).

## Installation

Include this in your build.gradle file:

```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.157239n:Progress:-SNAPSHOT'
}
```

## Simple usage

You can create a new Progress: `Progress progress = new Progress(0.5); // this means the progress is half way done`

You can then query it: `progress.get();`

You can set it: `progress.set(0.8); // this means the progress is almost done`

You can check whether it is done or not: `progress.isDone();`

## Complex usage

Let's say that you have task A, B and C. Task A is going to make the progress go from 0 to 0.5. Task B, 0.5 to 0.7 and task C, 0.7 to 1.0.

Let's say that inside task A there're also task A1 from 0 to 0.2, task A2 from 0.2 to 0.25, task A3 from 0.25 to 0.5. That is the same as saying A1 from 0 to 0.4, A2 from 0.4 to 0.5, A3 from 0.5 to 1 from task A's perspective. How would you encode this and make things work?
```
    Progress progress = new Progress(0.0);
    progress.pushRange(0.0, 0.5); // acknowledging that task A has range from 0.0 to 0.5
    progress.pushRange(0.0, 0.4); // acknowledging that task A1 has range from 0.0 to 0.4 from task A's perspective
    {
        // operations related to task A1
        progress.set(0.5); // progress is 0.5 from A1's perspective, which is 0.2 from A's perspective, and 0.1 from a global perspective
        // operations related to task A1
        progress.set(1.0); // acknowledge that task A1 is done, but the true progress is 0.2 from a global perspective
    }
    progress.popRange(); // saying that task A1 is over, we should now focus on task A
    progress.pushRange(0.4, 0.5); // acknowledging that task A2 has range from 0.4 to 0.5 from task A's perspective
    // do other operations....
```

Why do these kinds of convoluted operations? May be because you want to keep track of the progress of some nested
method, and you want the child methods to be tracked to concern only of themselves and nothing else:
```
    Progress progress = new Progress(0.0);
    int iterations = 3;
    for (int i = 0; i < iterations; i++) {
        progress.pushRange(1.0 * i / iterations, 1.0 * (i + 1) / iterations);
        otherMethod(progress);
        progress.popRange();
    }
```

For example here, you want to loop a method 3 times, each time with the method setting the progress itself.

## Further tools

There are a few other tools that I should mention about:

You can get an ASCII image of the progress bar: `progress.toString(30); // 30 here is the length`

You can tell the progress to draw itself onto the console until the progress reaches 100% (another thread must be controlling the Progress object): `progress.yieldUntilDone();`

You can get the integer percentagvalue: `progress.percentage();`