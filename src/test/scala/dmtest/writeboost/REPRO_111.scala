package dmtest.writeboost

import dmtest._
import dmtest.fs.EXT4
import dmtest.stack._

class REPRO_111 extends DMTestSuite {
  // not reproduced yet
  ignore("luks on top of writeboost") {
    slowDevice(Sector.G(10)) { backing =>
      fastDevice(Sector.G(1)) { caching =>
        Writeboost.sweepCaches(caching)
        val options = Map(
          "read_cache_threshold" -> 1
        )
        Writeboost.Table(backing, caching, options).create { wb =>
          Luks(wb) { s =>
            EXT4.format(s)
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
//            EXT4.Mount(s) { mp =>
//            }
//            EXT4.Mount(s) { mp =>
//            }
          }
        }
      }
    }
  }
  ignore("pattern verifier") {
    slowDevice(Sector.M(128)) { backing =>
      fastDevice(Sector.M(16)) { caching =>
        Writeboost.sweepCaches(caching)
        val options = Map(
          "read_cache_threshold" -> 1
        )
        Writeboost.Table(backing, caching, options).create  { wb =>
          Luks(wb) { s =>
            val ps = new RandomPatternVerifier(s, Sector.K(4))
            ps.stamp(5)
            assert(ps.verify())
            assert(ps.verify())
          }
        }
      }
    }
  }
  test("encrypted backing") {
    slowDevice(Sector.M(128)) { backing =>
      Luks(backing) { luks =>
        EXT4.format(luks)
        EXT4.Mount(luks) { mp =>
        }
        Shell(s"fsck.ext4 -fn ${luks.bdev.path}")
        fastDevice(Sector.M(16)) { caching =>
          Writeboost.sweepCaches(caching)
          val options = Map(
            "read_cache_threshold" -> 0
          )
          Writeboost.Table(backing, caching, options).create { s =>
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
            Shell(s"fsck.ext4 -fn ${s.bdev.path}")
          }
        }
      }
    }
  }
}
