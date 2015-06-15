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
package org.dataconservancy.dcs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;import java.lang.System;

/**
 * Dynamic array of bytes.
 */
public class ByteArray {

    public int length;

    public byte[] array;

    public ByteArray(int size) {
        this.array = new byte[size];
        this.length = 0;
    }

    /**
     * Takes ownership of array.
     *
     * @param array the array
     */
    public ByteArray(byte[] array) {
        this.array = array;
        this.length = array.length;
    }

    private void ensureCapacity(int min) {
        if (min > array.length) {
            int newlength;

            if (min < 2 * 1024 * 1024) {
                newlength = min << 1;
            } else {
                newlength = (min * 3) / 2;
            }

            byte[] newarray = new byte[newlength];
            System.arraycopy(array, 0, newarray, 0, array.length);
            array = newarray;
        }
    }

    public void append(byte value) {
        int index = length++;
        ensureCapacity(length);
        array[index] = value;
    }

    public void append(byte[] arr, int offset, int len) {
        int index = length;
        length += len;
        ensureCapacity(length);

        System.arraycopy(arr, 0, array, index, len);
    }

    public void append(InputStream is) throws IOException {
        for (; ; ) {
            int try_read = is.available();

            try_read = (try_read == 0 ? 4 * 1024 : try_read);

            ensureCapacity(length + try_read);

            int read = is.read(array, length, try_read);

            if (read == -1) {
                break;
            }

            length += read;
        }

    }

    public void insert(int index, byte value) {
        length++;
        ensureCapacity(length);

        if (index < length) {
            System
                    .arraycopy(array, index, array, index + 1, length - index
                            - 1);
        }

        array[index] = value;
    }

    public void remove(int index) {
        System.arraycopy(array, index + 1, array, index, --length - index);
    }

    public void removeFast(int index) {
        array[index] = array[--length];
    }

    public void removeFast(int index, int num) {
        length -= num;
        System.arraycopy(array, length, array, index, num);
    }

    public void resize(int size) {
        if (size > length) {
            ensureCapacity(size);
        }

        length = size;
    }

    public void copy(int src, int dest, int size) {
        System.arraycopy(array, src, array, dest, size);
    }

    public InputStream asInputStream() {
        return new InputStream() {

            int offset = 0;

            public int read() {
                if (offset == length) {
                    return -1;
                }

                return array[offset++] & 0xff;
            }

            public int available() {
                return length - offset;
            }

            public int read(byte[] b, int off, int len) {
                if (offset == length) {
                    return -1;
                }

                if (offset + len > length) {
                    len = length - offset;
                }

                System.arraycopy(array, offset, b, off, len);
                offset += len;

                return len;
            }
        };
    }

    public OutputStream asOutputStream() {
        return new OutputStream() {

            public void write(byte[] b, int off, int len) {
                append(b, off, len);
            }

            public void write(int b) {
                append((byte) b);
            }
        };
    }

}
