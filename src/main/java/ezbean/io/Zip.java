/**
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.io;

import static java.util.zip.ZipEntry.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * <p>
 * This class adds support for file name encodings other than UTF-8 (which is required to work on
 * ZIP files created by native zip tools and is able to skip a preamble like the one found in self
 * extracting archives.
 * </p>
 * <p>
 * It doesn't use <code>java.util.zip.ZipFile</code> as it would have to reimplement all methods
 * anyway. Like <code>java.util.ZipFile</code>, it uses RandomAccessFile under the covers and
 * supports compressed and uncompressed entries.
 * </p>
 * 
 * @see http://www.pkware.com/documents/casestudies/APPNOTE.TXT
 * @version 2008/12/03 13:54:46
 */
class Zip extends InputStream implements Archiver {

    /** The current processing archive. */
    private RandomAccessFile archive;

    /** The amount of remaining data for the current processing local file. */
    private long size;

    /** The flag for dummy. */
    private int dummy = -1;

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        if (size-- <= 0) {
            if (dummy == 0) {
                return dummy--;
            }
            return -1;
        }
        return archive.read();
    }

    // /**
    // * Get value as four bytes in big endian byte order.
    // *
    // * @param Avalue the value to convert
    // * @return A value as four bytes in big endian byte order
    // */
    // private static byte[] getBytes(long value) {
    // byte[] result = new byte[4];
    // result[0] = (byte) ((value & 0xFF));
    // result[1] = (byte) ((value & 0xFF00) >> 8);
    // result[2] = (byte) ((value & 0xFF0000) >> 16);
    // result[3] = (byte) ((value & 0xFF000000L) >> 24);
    // return result;
    // }

    /**
     * @see ezbean.io.Archiver#unpack(java.io.File, java.io.File)
     */
    public void unpack(File zip, File destination) throws IOException {
        archive = new RandomAccessFile(zip, "r");

        try {
            // Reads the central directory of this archive and populates the internal tables with
            // ZipEntry instances. The ZipEntrys will know all data that can be obtained from the
            // central directory alone, but not the data that requires the local file header or
            // additional data to be read.

            // Searches for End of Central Directory Record.
            //
            // 0 : end of central dir signature (4bytes 0x06054b50)
            // 4 : number of this disk (2bytes)
            // 6 : number of the disk with the start of the central directory (2bytes)
            // 8 : total number of entries in the central directory on this disk (2bytes)
            // 10 : total number of entries in the central directory (2bytes)
            // 12 : size of the central directory (4bytes)
            // 16 : relaive offset of start of central directory (4bytes)
            // 20 : .ZIP file comment length (2bytes)
            // 22 : .ZIP file comment (variable size)
            //
            // Minimum length of "End of Central Directory Record" is 22 bytes. (if no comments)
            long current = archive.length() - 22;

            while (0 <= current) {
                // move the current pointer to next search point
                archive.seek(current--);

                // The signature for the end of central directory is {80, 75, 5, 6}. This value is
                // computed by the follwing code.
                //
                // byte[] bytes = getBytes(0x06054b50L).
                if (archive.read() == 80 && archive.read() == 75 && archive.read() == 5 && archive.read() == 6) {
                    // Prepare data buffer of the central file header for reuse. Total length of the
                    // central file header is 42 bytes and signature of the central file header is
                    // 4 bytes.
                    byte[] header = new byte[42];
                    byte[] signature = new byte[4];

                    // move to the relative offset of the central directory
                    // 16 (for skip other header) + 1 (for decrement above)
                    archive.seek(16 + 1 + current);

                    // read relative offset of the central directory
                    archive.readFully(signature);

                    // move to the head of the central directory
                    archive.seek(getLong(signature, 0));

                    // read the signature of central file header
                    archive.readFully(signature);

                    // The signature for the central file header is 33639248L. This value is
                    // computed by the following code.
                    //
                    // byte[] bytes = getBytes(0x02014B50L);
                    // long signature = getLongValue(bytes, 0);
                    while (33639248L == getLong(signature, 0)) {
                        // read the central file header data actually
                        archive.readFully(header);

                        // We hardcode the offset for the central file header to reduce the
                        // footprint.
                        // 0 : Version Made By (2bytes)
                        // 2 : Version Needed to Extract (2bytes)
                        // 4 : General Purpose Bit Flag (2bytes)
                        // 6 : Compression Method (2bytes)
                        // 8 : File Time (4bytes)
                        // 12 : CRC32 (4bytes)
                        // 16 : Compressed Size (4bytes)
                        // 20 : Uncompressd Size (4bytes)
                        // 24 : File Name Length (2bytes)
                        // 26 : Extra Field Length (2bytes)
                        // 28 : File Comment Length (2bytes)
                        // 30 : Disk Number (2bytes)
                        // 32 : Internal File Attribute (2bytes)
                        // 34 : External File Attribute (4bytes)
                        // 38 : Relative Offset to Local File Header (4bytes)
                        // 
                        // File Name (variable size)
                        // Extra Field (variable size)
                        // File Comment (variable size)

                        // file name length
                        int length = getInt(header, 24);

                        // read File Name (variable size) and skip Extra Field (variable size) in
                        // central file header
                        byte[] data = new byte[length];
                        archive.readFully(data);
                        archive.skipBytes(getInt(header, 26));

                        // Decide encoding for file name and comment fields.
                        //
                        // General Purpose Bit Flag 11 means Language encoding flag (EFS). If
                        // this bit is set, the filename and comment fields for this file must be
                        // encoded using UTF-8. Otherwise they are encoded using the original ZIP
                        // character encoding.
                        // if ((getInt(header, 4) & 1 << 11) != 0) {
                        //
                        // } else {
                        // The upper byte indicates the compatibility of the file attribute
                        // information. If the external file attributes are compatible with
                        // MS-DOS and can be read by PKZIP for DOS version 2.04g then this value
                        // will be zero. If these attributes are not compatible, then this value
                        // will identify the host system on which the attributes are compatible.
                        // Software can use this information to determine the line record format
                        // for text files etc. The current mappings are:
                        //
                        // 0 - MS-DOS and OS/2 (FAT / VFAT / FAT32 file systems)
                        // 1 - Amiga
                        // 2 - OpenVMS
                        // 3 - UNIX
                        // 4 - VM/CMS
                        // 5 - Atari ST
                        // 6 - OS/2 H.P.F.S.
                        // 7 - Macintosh
                        // 8 - Z-System
                        // 9 - CP/M
                        // 10 - Windows NTFS
                        // 11 - MVS (OS/390 - Z/OS)
                        // 12 - VSE
                        // 13 - Acorn Risc
                        // 14 - VFAT
                        // 15 - alternate MVS
                        // 16 - BeOS
                        // 17 - Tandem
                        // 18 - OS/400
                        // 19 - OS/X (Darwin)
                        // 20 thru
                        // 255 - unused
                        // switch ((getInt(header, 0) >> 8) & 0x0f) {
                        // case 0:
                        // case 10:
                        // // Windows
                        // break;
                        //
                        // case 3:
                        // // Unix
                        // break;
                        //
                        // case 7:
                        // // Macintosh
                        // break;
                        //
                        // default:
                        // break;
                        // }
                        // }

                        // create unpacked file
                        String name = new String(data);
                        File file = new File(destination, name);

                        if (name.endsWith("/")) {
                            file.mkdirs();
                        } else {
                            // ensure an existence of parent directory
                            file.getParentFile().mkdirs();

                            // store current offset in central file header, we can read comment from
                            // next bytes (assign to "current" variable for reuse)
                            current = archive.getFilePointer();

                            // We hardcode the offset for the local file header to reduce the
                            // footprint.
                            // 0 : Local File Header Signature (4bytes)
                            // 4 : Version Info (2bytes)
                            // 6 : General Purpose Bytes (2bytes)
                            // 8 : Compression Method (2bytes)
                            // 10 : Last Modified Time (2bytes)
                            // 12 : Last Modified Date (2byte)
                            // 14 : CRC32 (4bytes)
                            // 18 : Compressed Size (4bytes)
                            // 22 : Uncompressed Size (4bytes)
                            // 26 : File Name Length (2bytes)
                            // 28 : Extra Field Length (2bytes)

                            // read Extra Field Length in local file header
                            data = new byte[2];
                            archive.seek(getLong(header, 38) + 28);
                            archive.readFully(data);

                            // skip File Name and Extra Field in local file header
                            archive.skipBytes(length + getInt(data, 0));

                            // transfer data and inflat if needed
                            InputStream input = this;
                            size = getLong(header, 16);

                            if (getInt(header, 6) == DEFLATED) {
                                // Note: When using the 'nowrap' option it is also necessary to
                                // provide an extra "dummy" byte as input. This is required by the
                                // ZLIB native library in order to support certain optimizations.
                                dummy = 0;

                                // Creates a new decompressor. If the parameter 'nowrap' is true
                                // then the ZLIB header and checksum fields will not be used. This
                                // provides compatibility with the compression format used by both
                                // GZIP and PKZIP.
                                input = new InflaterInputStream(input, new Inflater(true));
                            }
                            FileSystem.copy(input, new FileOutputStream(file));

                            // restore offset to the begining of File Comment in the central file
                            // header
                            archive.seek(current);
                        }

                        // read File Time (assign to "current" variable for reuse)
                        current = getLong(header, 8);

                        // Converts DOS time to Java time (number of milliseconds since epoch).
                        Calendar calendar = Calendar.getInstance();
                        calendar.set((int) (((current >> 25) & 0x7f) + 1980), (int) (((current >> 21) & 0x0f) - 1), (int) ((current >> 16) & 0x1f), (int) ((current >> 11) & 0x1f), (int) ((current >> 5) & 0x3f), (int) ((current << 1) & 0x3e));
                        calendar.set(Calendar.MILLISECOND, 0);

                        // synchronize the last modified date
                        file.setLastModified(calendar.getTimeInMillis());

                        // we must set permission at the last
                        file.setReadOnly();

                        // read the signature for next central file header
                        archive.readFully(signature);
                    }
                    return;
                }
            }
        } finally {
            archive.close();
        }
    }

    /**
     * Helper method to get the value as a java int from two bytes starting at given array offset
     * 
     * @param bytes the array of bytes
     * @param offset the offset to start
     * @return the correspondanding java int value
     */
    private static int getInt(byte[] bytes, int offset) {
        return ((bytes[offset + 1] << 8) & 0xFF00) + (bytes[offset] & 0xFF);
    }

    /**
     * Helper method to get the value as a Java long from four bytes starting at given array offset
     * 
     * @param bytes the array of bytes
     * @param offset the offset to start
     * @return the correspondanding Java long value
     */
    private static long getLong(byte[] bytes, int offset) {
        return ((bytes[offset + 3] << 24) & 0xFF000000L) + ((bytes[offset + 2] << 16) & 0xFF0000) + ((bytes[offset + 1] << 8) & 0xFF00) + ((bytes[offset] & 0xFF));
    }
}
