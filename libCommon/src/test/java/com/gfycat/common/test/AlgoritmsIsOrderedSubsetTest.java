/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Gfycat.
 *
 * As with any software that integrates with the Gfycat platform, your use of
 * this software is subject to the Gfycat Terms of Service [https://gfycat.com/terms]
 * and Partner Terms of Service [https://gfycat.com/partners/terms]. This copyright
 * notice shall be included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.gfycat.common.test;

import com.gfycat.common.utils.Algorithms;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by dekalo on 30.11.16.
 */

public class AlgoritmsIsOrderedSubsetTest extends Assert {

    @Test
    public void testAddToEmpty() throws Exception {
        success(0, 0, Algorithms.isOrderedSubset(Collections.emptyList(), Arrays.asList("1", "2", "3")));
    }

    @Test
    public void testSame() throws Exception {
        success(0, 3, Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3")));
    }

    @Test
    public void testAddToEndSubsetIsOne() throws Exception {
        success(0, 1, Algorithms.isOrderedSubset(Arrays.asList("1"), Arrays.asList("1", "2", "3", "4")));
    }

    @Test
    public void testAddToEnd() throws Exception {
        success(0, 3, Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3", "4")));
    }

    @Test
    public void testAddToStart() throws Exception {
        success(1, 4, Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("0", "1", "2", "3")));
    }

    @Test
    public void testAddToBoth() throws Exception {
        success(1, 4, Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("0", "1", "2", "3", "4")));
    }

    @Test
    public void testSmaller() throws Exception {
        fail(Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("5", "6")));
    }

    @Test
    public void testDifferent() throws Exception {
        fail(Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("5", "6", "7", "8")));
    }

    @Test
    public void testOneDifferentInTheMiddle() throws Exception {
        fail(Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("1", "22", "3")));
    }

    @Test
    public void testOneDifferentAtTheEnd() throws Exception {
        fail(Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "33")));
    }

    @Test
    public void testPartiallySame() throws Exception {
        fail(Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("0", "1", "2")));
    }

    @Test
    public void testCounts() throws Exception {
        Algorithms.IsOrderedSubsetResult result;
        result = Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("0", "1", "2", "3"));
        assertEquals(1, result.subsetStartIndex);
        assertEquals(0, 4 - result.subsetEndIndex);

        result = Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3", "4"));
        assertEquals(0, result.subsetStartIndex);
        assertEquals(3, result.subsetEndIndex);
        assertEquals(1, 4 - result.subsetEndIndex);

        result = Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("0", "1", "2", "3", "4"));
        assertEquals(1, result.subsetStartIndex);
        assertEquals(4, result.subsetEndIndex);
        assertEquals(1, 5 - result.subsetEndIndex);

        result = Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("0", "1", "2", "3", "4", "5"));
        assertEquals(1, result.subsetStartIndex);
        assertEquals(4, result.subsetEndIndex);
        assertEquals(2, 6 - result.subsetEndIndex);

        result = Algorithms.isOrderedSubset(Arrays.asList("1", "2", "3"), Arrays.asList("-1", "0", "1", "2", "3", "4", "5"));
        assertEquals(2, result.subsetStartIndex);
        assertEquals(5, result.subsetEndIndex);
        assertEquals(2, 7 - result.subsetEndIndex);
    }

    private void fail(Algorithms.IsOrderedSubsetResult result) {
        assertFalse("result different", result.isSubset);
    }

    private void success(int start, int end, Algorithms.IsOrderedSubsetResult result) {
        assertTrue("result different", result.isSubset);
        assertEquals("start different", start, result.subsetStartIndex);
        assertEquals("end different", end, result.subsetEndIndex);
    }

    @Test
    public void get100Coverage() throws Exception {
        new Algorithms();
    }

}
