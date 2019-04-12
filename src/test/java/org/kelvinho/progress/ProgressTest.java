package org.kelvinho.progress;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProgressTest {
    private static final double EPSILON = 0.00000001;

    @Test
    public void mainTest() {
        Progress progress = new Progress();
        progress.set(0.5);
        System.out.println(progress.toString());
        assertEquals(progress.getTrueValue(), 0.5);
        progress.pushRange(0.7, 0.8);
        progress.set(0.5);
        System.out.println(progress.toString());
        assertEquals(progress.getTrueValue(), 0.75);
        progress.popRange();
        progress.set(0.9);
        System.out.println(progress.toString());
        assertEquals(progress.getTrueValue(), 0.9);
        progress.set(1.0);
        System.out.println(progress.toString());
        assertTrue(progress.isDone());
    }

    private void assertEquals(double a, double b) {
        assertTrue(Math.abs(a - b) < EPSILON);
    }

    @Test
    public void yieldTest() {
        Progress progress = new Progress();
        /*
        {
            //Thread.yield();
            int partitions = 5;
            for (int i = 0; i < partitions; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    //System.out.println("Done waiting");
                    //Thread.yield();
                }
                //System.out.println("aa");
                progress.set((i + 1.0) / partitions);
            }
        }
        /**/
        new Thread(() -> {
            //Thread.yield();
            int partitions = 5;
            for (int i = 0; i < partitions; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    //System.out.println("Done waiting");
                    //Thread.yield();
                }
                //System.out.print("aa");
                progress.set((i + 1.0) / partitions);
                System.out.print("\r" + progress.toString());
            }
        }).start();/**/
        try {
            System.out.println(Thread.currentThread().getName() + " in control");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done sleeping in main");
        //progress.yieldUntilDone();
    }
}