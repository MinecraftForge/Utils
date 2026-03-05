/*
 * Copyright (c) Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.util.hash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class HashFunctionTest {
    private static final byte[] BULK_EMPTY = new byte[0x1000];
    private static final byte[] BULK_DATA = new byte[0x1000];
    static {
        for (int x = 0; x < BULK_DATA.length; x++)
            BULK_DATA[x] = (byte)(x & 0xFF);
    }

    private static void test(HashFunction func, String expected, String data) {
        Assertions.assertEquals(expected, func.hash(data));
    }

    private static void test(HashFunction func, String expected, byte[] data) {
        Assertions.assertEquals(expected, func.hash(data));
    }

    @Test
    public void alder32() {
        test(HashFunction.alder32(), "00000001", "");
        test(HashFunction.alder32(), "00620062", "a");
        test(HashFunction.alder32(), "024d0127", "abc");
        test(HashFunction.alder32(), "29750586", "message digest");
        test(HashFunction.alder32(), "90860b20", "abcdefghijklmnopqrstuvwxyz");
        test(HashFunction.alder32(), "5bdc0fda", "The quick brown fox jumps over the lazy dog");
        test(HashFunction.alder32(), "10000001", BULK_EMPTY);
        test(HashFunction.alder32(), "60aef86a", BULK_DATA);
    }

    @Test
    public void crc32() {
        test(HashFunction.crc32(), "00000000", "");
        test(HashFunction.crc32(), "e8b7be43", "a");
        test(HashFunction.crc32(), "352441c2", "abc");
        test(HashFunction.crc32(), "20159d7f", "message digest");
        test(HashFunction.crc32(), "4c2750bd", "abcdefghijklmnopqrstuvwxyz");
        test(HashFunction.crc32(), "414fa339", "The quick brown fox jumps over the lazy dog");
        test(HashFunction.crc32(), "c71c0011", BULK_EMPTY);
        test(HashFunction.crc32(), "a2912082", BULK_DATA);
    }

    @Test
    public void testMD5() {
        test(HashFunction.md5(), "d41d8cd98f00b204e9800998ecf8427e", "");
        test(HashFunction.md5(), "0cc175b9c0f1b6a831c399e269772661", "a");
        test(HashFunction.md5(), "900150983cd24fb0d6963f7d28e17f72", "abc");
        test(HashFunction.md5(), "f96b697d7cb7938d525a2f31aaf161d0", "message digest");
        test(HashFunction.md5(), "c3fcd3d76192e4007dfb496cca67e13b", "abcdefghijklmnopqrstuvwxyz");
        test(HashFunction.md5(), "9e107d9d372bb6826bd81d3542a419d6", "The quick brown fox jumps over the lazy dog");
        test(HashFunction.md5(), "620f0b67a91f7f74151bc5be745b7110", BULK_EMPTY);
        test(HashFunction.md5(), "2bcd3c4de20c918e19fab5c36249c70d", BULK_DATA);
    }

    @Test
    public void sha1() {
        test(HashFunction.sha1(), "da39a3ee5e6b4b0d3255bfef95601890afd80709", "");
        test(HashFunction.sha1(), "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8", "a");
        test(HashFunction.sha1(), "a9993e364706816aba3e25717850c26c9cd0d89d", "abc");
        test(HashFunction.sha1(), "c12252ceda8be8994d5fa0290a47231c1d16aae3", "message digest");
        test(HashFunction.sha1(), "32d10c7b8cf96570ca04ce37f2a19d84240d3a89", "abcdefghijklmnopqrstuvwxyz");
        test(HashFunction.sha1(), "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12", "The quick brown fox jumps over the lazy dog");
        test(HashFunction.sha1(), "1ceaf73df40e531df3bfb26b4fb7cd95fb7bff1d", BULK_EMPTY);
        test(HashFunction.sha1(), "e9dded8c84614e894501965af60c2525794a8c7d", BULK_DATA);
    }

    @Test
    public void sha256() {
        test(HashFunction.sha256(), "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", "");
        test(HashFunction.sha256(), "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb", "a");
        test(HashFunction.sha256(), "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", "abc");
        test(HashFunction.sha256(), "f7846f55cf23e14eebeab5b4e1550cad5b509e3348fbc4efa3a1413d393cb650", "message digest");
        test(HashFunction.sha256(), "71c480df93d6ae2f1efad1447c66c9525e316218cf51fc8d9ed832f2daf18b73", "abcdefghijklmnopqrstuvwxyz");
        test(HashFunction.sha256(), "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592", "The quick brown fox jumps over the lazy dog");
        test(HashFunction.sha256(), "ad7facb2586fc6e966c004d7d1d16b024f5805ff7cb47c7a85dabd8b48892ca7", BULK_EMPTY);
        test(HashFunction.sha256(), "c8f5d0341d54d951a71b136e6e2afcb14d11ed8489a7ae126a8fee0df6ecf193", BULK_DATA);
    }

    @Test
    public void sha512() {
        test(HashFunction.sha512(), "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e", "");
        test(HashFunction.sha512(), "1f40fc92da241694750979ee6cf582f2d5d7d28e18335de05abc54d0560e0f5302860c652bf08d560252aa5e74210546f369fbbbce8c12cfc7957b2652fe9a75", "a");
        test(HashFunction.sha512(), "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f", "abc");
        test(HashFunction.sha512(), "107dbf389d9e9f71a3a95f6c055b9251bc5268c2be16d6c13492ea45b0199f3309e16455ab1e96118e8a905d5597b72038ddb372a89826046de66687bb420e7c", "message digest");
        test(HashFunction.sha512(), "4dbff86cc2ca1bae1e16468a05cb9881c97f1753bce3619034898faa1aabe429955a1bf8ec483d7421fe3c1646613a59ed5441fb0f321389f77f48a879c7b1f1", "abcdefghijklmnopqrstuvwxyz");
        test(HashFunction.sha512(), "07e547d9586f6a73f73fbac0435ed76951218fb7d0c8d788a309d785436bbb642e93a252a954f23912547d1e8a3b5ed6e1bfd7097821233fa0538f3db854fee6", "The quick brown fox jumps over the lazy dog");
        test(HashFunction.sha512(), "2d23913d3759ef01704a86b4bee3ac8a29002313ecc98a7424425a78170f219577822fd77e4ae96313547696ad7d5949b58e12d5063ef2ee063b595740a3a12d", BULK_EMPTY);
        test(HashFunction.sha512(), "034a1bd3ad5dbddf6c9aed6b1705661487e110dc7e158fe330c94363e8ffb53b1c92f883010fd73ce8a86115b7b4712ba0f3a9279760ed6220a5773eb54425f0", BULK_DATA);
    }

    @Test
    public void bulk() throws IOException {
        File temp = File.createTempFile("hash-util-test", ".dat");
        Files.write(temp.toPath(), BULK_DATA);

        String[] hashes = HashUtils.bulkHash(temp,
            HashFunction.alder32(),
            HashFunction.crc32(),
            HashFunction.md5(),
            HashFunction.sha1(),
            HashFunction.sha256(),
            HashFunction.sha512()
        );

        Assertions.assertArrayEquals(new String[] {
            "60aef86a",
            "a2912082",
            "2bcd3c4de20c918e19fab5c36249c70d",
            "e9dded8c84614e894501965af60c2525794a8c7d",
            "c8f5d0341d54d951a71b136e6e2afcb14d11ed8489a7ae126a8fee0df6ecf193",
            "034a1bd3ad5dbddf6c9aed6b1705661487e110dc7e158fe330c94363e8ffb53b1c92f883010fd73ce8a86115b7b4712ba0f3a9279760ed6220a5773eb54425f0"
        }, hashes);
    }

    @Test
    public void updateHash(@TempDir File temp) throws IOException {
        File file = new File(temp, "hash-util-test.dat");
        Files.write(file.toPath(), BULK_DATA);
        updateHash(file, HashFunction.alder32(), "60aef86a");
        updateHash(file, HashFunction.crc32(),   "a2912082");
        updateHash(file, HashFunction.md5(),     "2bcd3c4de20c918e19fab5c36249c70d");
        updateHash(file, HashFunction.sha1(),    "e9dded8c84614e894501965af60c2525794a8c7d");
        updateHash(file, HashFunction.sha256(),  "c8f5d0341d54d951a71b136e6e2afcb14d11ed8489a7ae126a8fee0df6ecf193");
        updateHash(file, HashFunction.sha512(),  "034a1bd3ad5dbddf6c9aed6b1705661487e110dc7e158fe330c94363e8ffb53b1c92f883010fd73ce8a86115b7b4712ba0f3a9279760ed6220a5773eb54425f0");
    }

    private void updateHash(File file, HashFunction hash, String expected) throws IOException {
        File hashFile = new File(file.getAbsolutePath() + '.' + hash.extension());
        if (hashFile.exists())
            hashFile.delete();
        HashUtils.updateHash(file, hash);
        Assertions.assertTrue(hashFile.exists(), "Expected " + hashFile.getName() + " to exist");
        String existing = String.join("\n", Files.readAllLines(hashFile.toPath()));
        Assertions.assertEquals(expected, existing, hash.extension() + " did not write correct hash");
    }
}
