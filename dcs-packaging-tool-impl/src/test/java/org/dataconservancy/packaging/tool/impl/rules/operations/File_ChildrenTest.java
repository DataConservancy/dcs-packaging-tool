/*
 * Copyright 2014 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.packaging.tool.impl.rules.operations;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileContextImpl;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class File_ChildrenTest {
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    
    /* Verify that a regular file has no children */
    @Test
    public void regularFileTest() throws Exception {
        File file = tmpfolder.newFile("File_ChildrenTest");

        File_Children childOp = new File_Children();

        assertEquals(0, childOp.operate(new FileContextImpl(file, false)).length);
    }

    /* Verify that children of a directory will be returned */
    @Test
    public void directoryTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ChildrenTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();
        File file2 = new File(directory, "file2");
        file2.createNewFile();

        File_Children childOp = new File_Children();
        List<File> childFiles =
                Arrays.asList(childOp.operate(new FileContextImpl(directory, false)));

        assertTrue(childFiles.contains(file1));
        assertTrue(childFiles.contains(file2));
        assertEquals(2, childFiles.size());
    }

    /*
     * Verify that test operands are evaluated against all children, and
     * children are returned if tests pass
     */
    @Test
    public void filterChildrenTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ChildrenTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();
        File file2 = new File(directory, "file2");
        file2.createNewFile();

        TestOperation<?> filter1 = mock(TestOperation.class);
        TestOperation<?> filter2 = mock(TestOperation.class);

        when(filter1.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});
        when(filter2.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});

        File_Children childOp = new File_Children();
        childOp.setConstraints(filter1, filter2);

        List<File> childFiles =
                Arrays.asList(childOp.operate(new FileContextImpl(directory, false)));

        assertTrue(childFiles.contains(file1));
        assertTrue(childFiles.contains(file2));
        assertEquals(2, childFiles.size());

        /* Make sure filters called */
        verify(filter1).operate(new FileContextImpl(file1, false));
        verify(filter1).operate(new FileContextImpl(file2, false));
        verify(filter2).operate(new FileContextImpl(file1, false));
        verify(filter2).operate(new FileContextImpl(file2, false));

    }

    /*
     * Verify that test operands that return failure will cause child to be
     * omitted
     */
    @Test
    public void filterExcludeChildrenTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ChildrenTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();
        File file2 = new File(directory, "file2");
        file2.createNewFile();

        TestOperation<?> filter1 = mock(TestOperation.class);
        TestOperation<?> filter2 = mock(TestOperation.class);

        when(filter1.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});
        when(filter2.operate(new FileContextImpl(file1, false)))
                .thenReturn(new Boolean[] {true, false});
        when(filter2.operate(new FileContextImpl(file2, false)))
                .thenReturn(new Boolean[] {true, true});

        File_Children childOp = new File_Children();
        childOp.setConstraints(filter1, filter2);

        List<File> childFiles =
                Arrays.asList(childOp.operate(new FileContextImpl(directory, false)));

        assertFalse(childFiles.contains(file1));
        assertTrue(childFiles.contains(file2));
        assertEquals(1, childFiles.size());
    }

    /**
     * Test that exception is thrown when a non-readable directory is encountered.
     * @throws Exception
     */
    @Test (expected = OperationException.class)
    public void exceptionDirTest() throws Exception {
        //Set up directory
        File directory = tmpfolder.newFolder("File_ChildrenTest");

        //make dir non-readable
        if (directory.setReadable(false)) {

            //Mock constraints
            TestOperation<?> filter1 = mock(TestOperation.class);
            when(filter1.operate(any(FileContext.class))).thenReturn(new Boolean[]{
                    true, true});

            //create operation
            File_Children childOp = new File_Children();
            childOp.setConstraints(filter1);
            //perform operation, expects exception
            childOp.operate(new FileContextImpl(directory, false));
        } else {
            //TODO: This test doesn't work on windows becuase setReadable false doesn't work.
            throw new OperationException("Failed to make directory unreadable");
        }
    }
}
