package dmtest.writeboost

import dmtest.DMTestSuite

import dmtest._
import dmtest.stack._
import dmtest.fs._

class ScenarioTest extends DMTestSuite {
  test("compile ruby") {
    slowDevice(Sector.G(4)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          XFS.format(s)
          XFS.Mount(s) { mp =>
            val rc = new CompileRuby(mp)
            rc.downloadArchive
            rc.unarchive
          }
        }
        Writeboost.Table(backing, caching, Map("writeback_threshold" -> 70, "read_cache_threshold" -> 31)).create { s =>
          s.dropCaches()
          XFS.Mount(s) { mp =>
            val rc = new CompileRuby(mp)
            rc.compile
            rc.check
          }
        }
      }
    }
  }
  test("stress") {
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(128)) { caching =>
        Writeboost.sweepCaches(caching)
        Writeboost.Table(backing, caching).create { s =>
          XFS.format(s)
          XFS.Mount(s) { mp =>
            Shell.at(mp) { s"stress -v --timeout ${60 <> 1} --hdd 4 --hdd-bytes 512M"}
          }
        }
      }
    }
  }
  test("dbench") {
    slowDevice(Sector.G(2)) { backing =>
      fastDevice(Sector.M(64)) { caching =>
        Writeboost.sweepCaches(caching)
        val table = Writeboost.Table(backing, caching,
          Map("writeback_threshold" -> 70, "read_cache_threshold" -> 31)
        )
        def run(option: String): Unit = {
          table.create { s =>
            XFS.format(s)
            XFS.Mount(s) { mp =>
              Shell.at(mp)(s"dbench ${option}")
              Shell("sync")
              Kernel.dropCaches
              s.dropCaches()
            }
          }
        }
        val t = 300 <> 1
        run(s"-t ${t} 4")
        run(s"-S -t ${t} 4") // directory operations are sync
        run(s"-s -t ${t} 4") // all operations are sync
      }
    }
  }
}