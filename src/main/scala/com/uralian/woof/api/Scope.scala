package com.uralian.woof.api

import org.json4s._

/**
 * Component of a query scope: can be a tag, or a tag name, or a variable name.
 * Eg. "foo:bar", "host", "$client".
 */
sealed trait ScopeElement

/**
 * Factory for ScopeElement instances.
 */
object ScopeElement {
  /**
   * Creates a new ScopeElement instance from a string.
   *
   * @param str a string that is either a tag, a tag name, or a variable name.
   * @return a new scope element.
   */
  def apply(str: String): ScopeElement =
    if (str startsWith "$") VarName(str) else if (str contains ':') Tag(str) else TagName(str)

  /**
   * JSON serializer for scope elements.
   */
  val serializer: CustomSerializer[ScopeElement] = new CustomSerializer[ScopeElement](_ => ( {
    case JString(str) => apply(str)
  }, {
    case element: ScopeElement => JString(element.toString)
  }))
}

/**
 * A DataDog tag.
 *
 * @param name  tag name.
 * @param value tag value.
 */
final case class Tag private(name: String, value: String) extends ScopeElement {
  require(Tag.isValidTagName(name), "invalid tag name")
  require(Tag.isValidTagValue(value), "invalid tag value")
  require(name.length + value.size <= 200, "tag size too long")

  override val toString: String = name + ":" + value
}

/**
 * Tag instance factory.
 */
object Tag {

  private val tagNameRegex = """[a-z0-9_\-\./]+""".r

  private val tagValueRegex = """[a-z0-9:_\-\./]+""".r

  /**
   * Tests if the string is a valid tag name.
   *
   * @param str string to check.
   * @return `true` if the argument is a valid tag name.
   */
  def isValidTagName(str: String) = tagNameRegex.pattern.matcher(str).matches

  /**
   * Tests if the string is a valid tag value.
   *
   * @param str string to check.
   * @return `true` if the argument is a valid tag value.
   */
  def isValidTagValue(str: String) = tagValueRegex.pattern.matcher(str).matches

  /**
   * Creates a new Tag instance converting the name and value into lowercase.
   *
   * @param name  tag name.
   * @param value tag value.
   * @return a new Tag instance.
   */
  def apply(name: String, value: String): Tag = new Tag(name.toLowerCase, value.toLowerCase)

  /**
   * Creates a new Tag instance from "name:value" formatted string.
   *
   * @param str string in the format "name:value".
   * @return a new Tag.
   */
  def apply(str: String): Tag = {
    val parts = str.split(':').toList
    Tag(parts.head, parts.tail.mkString(":"))
  }

  /**
   * Tag JSON serializer.
   */
  val serializer: CustomSerializer[Tag] = new CustomSerializer[Tag](_ => ( {
    case JString(str) => Tag(str)
  }, {
    case tag: Tag => JString(tag.toString)
  }))
}

/**
 * A DataDog tag name.
 *
 * @param name tag name.
 */
final case class TagName(name: String) extends ScopeElement {
  require(Tag.isValidTagName(name), "invalid tag name")

  override def toString: String = name
}

/**
 * Instance factory for [[TagName]].
 */
object TagName {
  /**
   * TagName JSON serializer.
   */
  val serializer: CustomSerializer[TagName] = new CustomSerializer[TagName](_ => ( {
    case JString(str) => TagName(str)
  }, {
    case TagName(name) => JString(name)
  }))
}

/**
 * A DataDog variable name.
 *
 * @param name variable name, should start with "$".
 */
final case class VarName(name: String) extends ScopeElement {
  require(VarName.isValidVarName(name), "invalid var name")

  override def toString: String = name
}

/**
 * Instance factory for [[VagName]].
 */
object VarName {

  private val varNameRegex = """\$[a-z0-9_\-\./]+""".r

  /**
   * Checks if the supplied string is a valid template variable name.
   *
   * @param str string to check.
   * @return `true` if the argument is a valid var name.
   */
  def isValidVarName(str: String) = varNameRegex.pattern.matcher(str).matches

  /**
   * VagName JSON serializer.
   */
  val serializer: CustomSerializer[VarName] = new CustomSerializer[VarName](_ => ( {
    case JString(str) => VarName(str)
  }, {
    case VarName(name) => JString(name)
  }))
}

/**
 * Metric query scope.
 */
sealed trait Scope

/**
 * Scope options.
 */
object Scope {

  val AllToken = "*"

  /**
   * Unconstrained scope, no filtering.
   */
  final case object All extends Scope {
    override def toString: String = AllToken
  }

  /**
   * Filter by tags, tag names or variables.
   *
   * @param elements
   */
  final case class Filter(elements: ScopeElement*) extends Scope {
    override def toString: String = elements.mkString(",")
  }

  /**
   * Factory for [[Filter]] instances.
   */
  object Filter {
    def apply(str: String): Filter = apply(str.split(",").map(_.trim).map(ScopeElement.apply).toSeq: _*)
  }

  /**
   * Creates a new Scope from the supplied string.
   *
   * @param str either "*" or a list of scope elements - tags, names and variables.
   * @return a new Scope instance.
   */
  def apply(str: String): Scope = str match {
    case AllToken => All
    case elements => Filter(elements)
  }

  /**
   * JSON serializer for scope instances.
   */
  val serializer: CustomSerializer[Scope] = new CustomSerializer[Scope](_ => ( {
    case JString(str) => apply(str)
  }, {
    case All       => JString(All.toString)
    case f: Filter => JString(f.toString)
  }))
}