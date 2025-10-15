package com.example.lzh;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.example.lzh.model.LzhEntryTest;
import com.example.lzh.model.LzhArchiveTest;
import com.example.lzh.exception.LzhExceptionTest;
import com.example.lzh.decompressor.LzhDecompressorTest;
import com.example.lzh.decompressor.Lh0DecompressorTest;

/**
 * Simple test runner for basic unit tests
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("Running LZH Library Unit Tests...");
        
        JUnitCore junit = new JUnitCore();
        
        // Run basic unit tests that don't require Android
        Class<?>[] testClasses = {
            LzhEntryTest.class,
            LzhArchiveTest.class,
            LzhExceptionTest.class,
            LzhDecompressorTest.class,
            Lh0DecompressorTest.class
        };
        
        int totalTests = 0;
        int totalFailures = 0;
        
        for (Class<?> testClass : testClasses) {
            System.out.println("\n--- Running " + testClass.getSimpleName() + " ---");
            
            Result result = junit.run(testClass);
            
            totalTests += result.getRunCount();
            totalFailures += result.getFailureCount();
            
            System.out.println("Tests run: " + result.getRunCount());
            System.out.println("Failures: " + result.getFailureCount());
            System.out.println("Ignored: " + result.getIgnoreCount());
            System.out.println("Time: " + result.getRunTime() + "ms");
            
            if (result.getFailureCount() > 0) {
                System.out.println("Failures:");
                for (Failure failure : result.getFailures()) {
                    System.out.println("  " + failure.getTestHeader());
                    System.out.println("    " + failure.getMessage());
                }
            }
        }
        
        System.out.println("\n=== Test Summary ===");
        System.out.println("Total tests: " + totalTests);
        System.out.println("Total failures: " + totalFailures);
        System.out.println("Success rate: " + ((totalTests - totalFailures) * 100.0 / totalTests) + "%");
        
        if (totalFailures == 0) {
            System.out.println("All tests passed!");
        } else {
            System.out.println("Some tests failed. Check the output above for details.");
        }
    }
}