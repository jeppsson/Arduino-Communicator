/*
 * Copyright (C) 2012 Mathias Jeppsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.primavera.arduino.listener;

class ByteArray {

    private byte[] mByteArray = new byte[1];
    private int mUsedLength;
    private boolean mShowInAscii;

    void add(byte[] newArray) {
        // Make sure we have enough space to store byte array.
        while (mUsedLength + newArray.length > mByteArray.length) {
            byte[] tmpArray = new byte[mByteArray.length * 2];
            System.arraycopy(mByteArray, 0, tmpArray, 0, mUsedLength);
            mByteArray = tmpArray;
        }

        // Add byte array.
        System.arraycopy(newArray, 0, mByteArray, mUsedLength, newArray.length);
        mUsedLength += newArray.length;
    }

    void toggleCoding() {
        mShowInAscii = !mShowInAscii;
    }

    @Override
    public String toString() {
        StringBuilder hexStr = new StringBuilder();

        if (mShowInAscii) {
            for (int i = 0; i < mUsedLength; i++) {
                if (Character.isLetterOrDigit(mByteArray[i])) {
                    hexStr.append(new String(new byte[] {mByteArray[i]}));
                } else {
                    hexStr.append('.');
                }
            }
        } else {
            for (int i = 0; i < mUsedLength; i++) {
                hexStr.append(String.format("%1$02X", mByteArray[i]));
                hexStr.append(" ");
            }
        }

        return hexStr.toString();
    }
}