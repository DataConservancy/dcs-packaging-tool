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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class File_AncestorsTest {
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    /* Verify that ancestors are found */
    @Test
    public void ancestorTest() throws Exception {

        File directory = tmpfolder.newFolder("File_AncestorsTest");
        File subdir = mkdir(directory, "subdir");
        File subsubdir = mkdir(subdir, "subsubdir");

        File file = new File(subsubdir, "file");
        file.createNewFile();
        file.deleteOnExit();

        File_Ancestors ancestorOp = new File_Ancestors();

        List<File> ancestors =
                Arrays.asList(ancestorOp.operate(new FileContextImpl(file, false)));
        assertTrue(ancestors.contains(subsubdir));
        assertTrue(ancestors.contains(subdir));
        assertTrue(ancestors.contains(directory));
        assertFalse(ancestors.contains(file));
    }

    /* Verify that we stop at a particular root, if given */
    @Test
    public void rootTest() throws Exception {
        File directory = tmpfolder.newFolder("File_AncestorsTest");

        File subdir = mkdir(directory, "subdir");
        File subsubdir = mkdir(subdir, "subsubdir");

        File file = new File(subsubdir, "file");
        file.createNewFile();
        file.deleteOnExit();

        File_Ancestors ancestorOp = new File_Ancestors();

        assertTrue(Arrays.equals(ancestorOp.operate(new FileContextImpl(file, subdir, false)),
                                 new File[] {subsubdir, subdir}));
    }

    /* Verify that filters are applied */
    @Test
    public void filterTest() throws Exception {
        File directory = tmpfolder.newFolder("File_AncestorsTest");

        File subdir = mkdir(directory, "subdir");
        File subsubdir = mkdir(subdir, "subsubdir");

        File file = new File(subsubdir, "file");
        file.createNewFile();
        file.deleteOnExit();

        TestOperation<?> filter1 = mock(TestOperation.class);
        TestOperation<?> filter2 = mock(TestOperation.class);

        when(filter1.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});
        when(filter2.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});

        File_Ancestors ancestorOp = new File_Ancestors();
        ancestorOp.setConstraints(filter1, filter2);

        assertTrue(Arrays.equals(ancestorOp.operate(new FileContextImpl(file, subdir, false)),
                                 new File[] {subsubdir, subdir}));

        verify(filter1).operate(new FileContextImpl(subdir, subdir, false));
        verify(filter2).operate(new FileContextImpl(subdir, subdir, false));
        verify(filter1).operate(new FileContextImpl(subsubdir, subdir, false));
        verify(filter2).operate(new FileContextImpl(subsubdir, subdir, false));

        verify(filter1, times(2)).operate(any(FileContext.class));
        verify(filter2, times(2)).operate(any(FileContext.class));
    }

    /*
     * Verify that false values in filters cause ancestor traversal to stop at
     * that point
     */
    @Test
    public void rejectTest() throws Exception {
        File directory = tmpfolder.newFolder("File_AncestorsTest");

        File subdir = mkdir(directory, "XXXsubdir");
        File subsubdir = mkdir(subdir, "subsubdir");

        File file = new File(subsubdir, "file");
        file.createNewFile();
        file.deleteOnExit();

        TestOperation<?> filter1 = mock(TestOperation.class);
        TestOperation<?> filter2 = mock(TestOperation.class);

        /* 'subdir' will return a false value */
        when(filter1.operate(new FileContextImpl(subdir, false)))
                .thenReturn(new Boolean[] {true, true});
        when(filter2.operate(new FileContextImpl(subdir, false)))
                .thenReturn(new Boolean[] {true, false});
        when(filter1.operate(new FileContextImpl(subsubdir, false)))
                .thenReturn(new Boolean[] {true, true});
        when(filter2.operate(new FileContextImpl(subsubdir, false)))
                .thenReturn(new Boolean[] {true, true});

        File_Ancestors ancestorOp = new File_Ancestors();
        ancestorOp.setConstraints(filter1, filter2);

        /* Our final array should contain only subsubdir */
        assertTrue(Arrays.equals(ancestorOp.operate(new FileContextImpl(file, false)),
                                 new File[] {subsubdir}));

    }

    private File mkdir(File parent, String name) throws Exception {
        File directory = new File(parent, name);
        directory.mkdir();
        directory.deleteOnExit();

        return directory;
    }

    /* Verify that attempt to find ancestor for a non-readable directory will throw OperationException */
    @Test (expected = OperationException.class)
    public void ancestorNonReadableExceptionTest() throws Exception {
        File directory = tmpfolder.newFolder("File_AncestorsTest");

        File subdir = mkdir(directory, "subdir");
        File subsubdir = mkdir(subdir, "subsubdir");

        File file = new File(subsubdir, "file");
        file.delete();

        File_Ancestors ancestorOp = new File_Ancestors();

        ancestorOp.operate(new FileContextImpl(file, false));
    }

}
