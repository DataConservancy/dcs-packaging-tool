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

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileContextImpl;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class File_ParentTest {
    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    /* Verify that simple parentage works */
    @Test
    public void parentTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ParentTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();

        File_Parent parentOp = new File_Parent();
        assertTrue(Arrays.equals(parentOp.operate(new FileContextImpl(file1, false)),
                                 new File[] {directory}));

    }

    /* Verify that a root directory has no parents */
    @Test
    public void rootTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ParentTest");
        
        File file1 = new File(directory, "file1");
        file1.createNewFile();

        File_Parent parentOp = new File_Parent();
        assertTrue(Arrays.equals(parentOp.operate(new FileContextImpl(file1,
                                                                      file1, false)),
                                 new File[] {}));
    }

    /* Verify that filters work */
    @Test
    public void filterTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ParentTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();

        TestOperation<?> filter1 = mock(TestOperation.class);
        TestOperation<?> filter2 = mock(TestOperation.class);

        when(filter1.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});
        when(filter2.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});

        File_Parent parentOp = new File_Parent();
        parentOp.setConstraints(filter1, filter2);

        assertTrue(Arrays.equals(parentOp.operate(new FileContextImpl(file1, false)),
                                 new File[] {directory}));

        verify(filter1).operate(new FileContextImpl(directory, false));
        verify(filter2).operate(new FileContextImpl(directory, false));
    }

    /* Verify that filters that return false result in empty file array */
    @Test
    public void falseTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ParentTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();

        TestOperation<?> filter1 = mock(TestOperation.class);
        TestOperation<?> filter2 = mock(TestOperation.class);

        when(filter1.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, true});
        when(filter2.operate(any(FileContext.class))).thenReturn(new Boolean[] {
                true, false});

        File_Parent parentOp = new File_Parent();
        parentOp.setConstraints(filter1, filter2);

        assertTrue(Arrays.equals(parentOp.operate(new FileContextImpl(file1, false)),
                                 new File[] {}));
    }

    /* Verify that attempt to find parent for a non-readable pathname will throw OperationException */
    @Test (expected = OperationException.class)
    public void parentNonReadableFileExceptionTest() throws Exception {
        File directory = tmpfolder.newFolder("File_ParentTest");

        File file1 = new File(directory, "file1");
        file1.createNewFile();
        file1.delete();

        File_Parent parentOp = new File_Parent();
        parentOp.operate(new FileContextImpl(file1, false));

    }
}
