from java2ta.abstraction import AbstractAttribute, StateSpace
from java2ta.abstraction.domains import Domain, Variable, INTEGERS, Integer, split_numeric_domain

def test_abstract_attribute():

    values = [ "< 0", "== 0", "> 0" ]
    initial = "== 0"

    aa = AbstractAttribute("foo", values, initial)

    assert len(aa.encoded_values) == len(values)
    assert aa.values == sorted(values), "%s vs %s" % (aa.values, values)
    assert aa.initial != initial
    assert aa.initial in aa.encoded_values


def test_statespace():

    values_foo = [ "< 0", "== 0", "> 0" ]
    initial_foo = "== 0"

    foo = AbstractAttribute("foo", values_foo, initial_foo)

    values_fie = [ "A", "B", "C", "D", "[E-Z]" ]
    initial_fie = "A"

    fie = AbstractAttribute("fie", values_fie, initial_fie)

    ss = StateSpace()
    ss.add_attribute(foo)
    ss.add_attribute(fie)

    # check attributes are returned sorted by name
    assert ss.attributes == [ fie, foo ]
    # check proper attributes are returned
    assert ss.get_attribute("foo") == foo
    assert ss.get_attribute("fie") == fie

    assert isinstance(ss.enumerate, list), "Expected list. Got: %s" % ss.enumerate

    assert len(ss.enumerate) == len(values_foo) * len(values_fie)
    assert (fie.encoded_value("A"), foo.encoded_value("> 0")) in ss.enumerate, ss.enumerate
    assert (fie.encoded_value("A"), foo.encoded_value("== 0")) in ss.enumerate
    assert ("A", "> 0") not in ss.enumerate
    assert ("A", "== 0") not in ss.enumerate
    assert ("A", "B") not in ss.enumerate
    assert ("> 0", "== 0") not in ss.enumerate

    assert ss.value((0,0)) == ("A", "< 0"), ss.value((0,0))
    assert ss.value((0,1)) == ("A", "== 0")
    assert ss.value((1,2)) == ("B", "> 0")

    assert ss.configuration(["A", "< 0"]) == (0,0)
    assert ss.configuration(["A", "== 0"]) == (0,1)
    assert ss.configuration(["A", "> 0"]) == (0,2)

    assert ss.configuration(["C", "< 0"]) == (2,0)
    assert ss.configuration(["C", "== 0"]) == (2,1)
    assert ss.configuration(["C", "> 0"]) == (2,2)


def test_statespace_from_variables():

    foo_domain = INTEGERS
    foo_predicates = foo_domain.predicates
    var_foo = Variable("foo", domain=foo_domain)

    abstract_values_foo = var_foo.values
    abstract_initial_foo = var_foo.default

    foo = AbstractAttribute("foo", abstract_values_foo, abstract_initial_foo)

    fie_datatype = Integer()
    fie_predicates = split_numeric_domain([-5,0,10], gt_max=False)
    var_fie = Variable("fie", datatype=fie_datatype, predicates=fie_predicates)

    abstract_values_fie = var_fie.values
    abstract_initial_fie = var_fie.default

    fie = AbstractAttribute("fie", abstract_values_fie, abstract_initial_fie)

    ss = StateSpace()
    ss.add_attribute(foo)
    ss.add_attribute(fie)

    # check attributes are returned sorted by name
    assert ss.attributes == [ fie, foo ]
    # check proper attributes are returned
    assert ss.get_attribute("foo") == foo
    assert ss.get_attribute("fie") == fie

    assert isinstance(ss.enumerate, list), "Expected list. Got: %s" % ss.enumerate

    assert len(ss.enumerate) == len(abstract_values_foo) * len(abstract_values_fie)
    assert (fie.encoded_value(fie_predicates[0]), foo.encoded_value(foo_predicates[0])) in ss.enumerate
    assert (fie.encoded_value(fie_predicates[0]), foo.encoded_value(foo_predicates[1])) in ss.enumerate
    assert (fie.encoded_value(fie_predicates[1]), foo.encoded_value(foo_predicates[0])) in ss.enumerate

    assert (fie_predicates[0], foo_predicates[0]) not in ss.enumerate
    assert (fie_predicates[0], foo_predicates[1]) not in ss.enumerate
    assert (fie_predicates[1], foo_predicates[0]) not in ss.enumerate

    assert ss.configuration([fie_predicates[0], foo_predicates[0]]) == (fie.encoded_value(fie_predicates[0]),foo.encoded_value(foo_predicates[0]))
    assert ss.configuration([fie_predicates[0], foo_predicates[1]]) == (fie.encoded_value(fie_predicates[0]),foo.encoded_value(foo_predicates[1]))
    assert ss.configuration([fie_predicates[1], foo_predicates[0]]) == (fie.encoded_value(fie_predicates[1]),foo.encoded_value(foo_predicates[0]))
