package com.github.jokerpper.hierarchy;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class HierarchyHelperTest {

    @Test(expected = IllegalArgumentException.class)
    public void checkRootListByEmptyList() {
        HierarchyHelper.checkRootList(Collections.emptyList());
    }

    @Test
    public void checkRootList() {
        HierarchyHelper.checkRootList(Arrays.asList(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkRootListByMore() {
        HierarchyHelper.checkRootList(Arrays.asList(1, 2));
    }


}