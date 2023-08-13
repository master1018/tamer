@TestTargetClass(StatFs.class)
public class StatFsTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StatFs",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restat",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBlockSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBlockCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFreeBlocks",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAvailableBlocks",
            args = {}
        )
    })
    public void testStatFs(){
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        int blockSize = stat.getBlockSize();
        int totalBlocks = stat.getBlockCount();
        int freeBlocks = stat.getFreeBlocks();
        int availableBlocks = stat.getAvailableBlocks();
        assertTrue(blockSize > 0);
        assertTrue(totalBlocks > 0);
        assertTrue(freeBlocks >= availableBlocks);
        assertTrue(availableBlocks > 0);
        path = Environment.getRootDirectory();
        stat.restat(path.getPath());
        blockSize = stat.getBlockSize();
        totalBlocks = stat.getBlockCount();
        freeBlocks = stat.getFreeBlocks();
        availableBlocks = stat.getAvailableBlocks();
        assertTrue(blockSize > 0);
        assertTrue(totalBlocks > 0);
        assertTrue(freeBlocks >= availableBlocks);
        assertTrue(availableBlocks > 0);
    }
}
