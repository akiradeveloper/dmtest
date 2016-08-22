package dmtest.writeboost

import dmtest._
import dmtest.stack._

class REPRO_144 extends DMTestSuite {
  test("log rotated and superblock record is enabled") {
    Memory(Sector.M(128)) { backing =>
      Memory(Sector.M(32)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          s.bdev.write(Sector(0), DataBuffer.random(Sector.M(64).toB.toInt))
          s.dropCaches()
          assert(s.status.lastFlushedId === s.status.lastWritebackId + 1)
        }
        // this should not cause kernel panic
        Writeboost.Table(backing, caching).create { s =>
        }
      }
    }
  }
}
