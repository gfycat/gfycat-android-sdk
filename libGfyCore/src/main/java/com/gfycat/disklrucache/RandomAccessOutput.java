/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
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

package com.gfycat.disklrucache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by dekalo on 21.10.15.
 */
public class RandomAccessOutput extends OutputStream {

  private RandomAccessFile randomAccessFile;

  public RandomAccessOutput() {
  }

  public RandomAccessOutput(File file) throws FileNotFoundException {
    this.randomAccessFile = new RandomAccessFile(file, "rw");
  }

  public long length() throws IOException{
    if(randomAccessFile != null) return randomAccessFile.length();
    return 0;
  }

  public void seek(long pos) throws IOException {
    if(randomAccessFile != null) randomAccessFile.seek(pos);
  }

  @Override
  public void write(int b) throws IOException {
    if(randomAccessFile != null) randomAccessFile.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    if(randomAccessFile != null) randomAccessFile.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if(randomAccessFile != null) randomAccessFile.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    if(randomAccessFile != null) randomAccessFile.getFD().sync();
  }

  @Override
  public void close() throws IOException {
    if(randomAccessFile != null) randomAccessFile.close();
  }
}
