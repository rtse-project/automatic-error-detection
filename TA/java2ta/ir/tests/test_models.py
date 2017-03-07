from time import sleep

from java2ta.ir.models import Project

def test_open_project():

    p = Project("fooproject", "file:///home/spegni/git/safe-rtse/java-xal/TA/java2ta/ir/tests/javaproject/", "localhost:9000")

    assert p.is_status("closed")
    p.open()

    sleep(1)

    assert p.is_status("opening") or p.is_status("open")


def test_close_wrong_project():

    p = Project("foofiedoesnotexist", "file:///home/spegni/git/safe-rtse/java-xal/TA/java2ta/ir/tests/javaproject/", "localhost:9000")

    try:
        p.clean()
        assert False, "An exception was expected because it does not exist such a project"
    except Exception:
        # this was expected

def test_close_open_project():

     p = Project("fooproject", "file:///home/spegni/git/safe-rtse/java-xal/TA/java2ta/ir/tests/javaproject/", "localhost:9000")

    p.open()

    sleep(1)

    assert p.is_status("opening") or p.is_status("open")
   
    p.clean()

    assert p.is_status("closed")
