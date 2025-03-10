package scala.meta
package tokens

import scala.math.ScalaNumber
import scala.meta.internal.tokens._
import scala.meta.inputs._
import scala.meta.classifiers._
import scala.meta.prettyprinters._
import scala.meta.internal.prettyprinters._

// NOTE: `start` and `end` are String.substring-style,
// i.e. `start` is inclusive and `end` is not.
// Therefore Token.end can point to the last character plus one.
// Btw, Token.start can also point to the last character plus one if it's an EOF token.
@root trait Token extends InternalToken {
  def input: Input
  def dialect: Dialect
  def start: Int
  def end: Int
  def pos: Position
  def text: String = pos.text

  final def isEmpty: Boolean = start == end
}

object Token {
  // Literals (include some keywords from above, constants, interpolations and xml)
  @branch trait Literal extends Token
  @branch abstract class Constant[A] extends Literal {
    val value: A
  }
  @branch abstract class NumericConstant[A <: ScalaNumber] extends Constant[A]

  @branch trait Keyword extends Token
  @branch trait ModifierKeyword extends Keyword

  @branch trait Trivia extends Token
  @branch trait Whitespace extends Trivia
  @branch trait HSpace extends Whitespace
  @branch trait AtEOL extends Whitespace
  @branch trait EOL extends AtEOL

  @branch trait Symbolic extends Token
  @branch trait SymbolicKeyword extends Symbolic
  @branch trait Punct extends Symbolic
  @branch trait OpenDelim extends Punct
  @branch trait CloseDelim extends Punct

  // Identifiers
  @freeform("identifier") class Ident(value: String) extends Token

  // Alphanumeric keywords
  @fixed("abstract") class KwAbstract extends ModifierKeyword
  @fixed("case") class KwCase extends Keyword
  @fixed("catch") class KwCatch extends Keyword
  @fixed("class") class KwClass extends Keyword
  @fixed("def") class KwDef extends Keyword
  @fixed("do") class KwDo extends Keyword
  @fixed("else") class KwElse extends Keyword
  @fixed("enum") class KwEnum extends Keyword
  @fixed("export") class KwExport extends Keyword
  @fixed("extends") class KwExtends extends Keyword
  @fixed("false") class KwFalse extends Literal
  @fixed("final") class KwFinal extends ModifierKeyword
  @fixed("finally") class KwFinally extends Keyword
  @fixed("for") class KwFor extends Keyword
  @fixed("forSome") class KwForsome extends Keyword
  @fixed("given") class KwGiven extends Keyword
  @fixed("if") class KwIf extends Keyword
  @fixed("implicit") class KwImplicit extends ModifierKeyword
  @fixed("import") class KwImport extends Keyword
  @fixed("lazy") class KwLazy extends ModifierKeyword
  @fixed("match") class KwMatch extends Keyword
  @fixed("macro") class KwMacro extends Keyword
  @fixed("new") class KwNew extends Keyword
  @fixed("null") class KwNull extends Literal
  @fixed("object") class KwObject extends Keyword
  @fixed("override") class KwOverride extends ModifierKeyword
  @fixed("package") class KwPackage extends Keyword
  @fixed("private") class KwPrivate extends ModifierKeyword
  @fixed("protected") class KwProtected extends ModifierKeyword
  @fixed("return") class KwReturn extends Keyword
  @fixed("sealed") class KwSealed extends ModifierKeyword
  @fixed("super") class KwSuper extends Keyword
  @fixed("then") class KwThen extends Keyword
  @fixed("this") class KwThis extends Keyword
  @fixed("throw") class KwThrow extends Keyword
  @fixed("trait") class KwTrait extends Keyword
  @fixed("true") class KwTrue extends Literal
  @fixed("try") class KwTry extends Keyword
  @fixed("type") class KwType extends Keyword
  @fixed("val") class KwVal extends Keyword
  @fixed("var") class KwVar extends Keyword
  @fixed("while") class KwWhile extends Keyword
  @fixed("with") class KwWith extends Keyword
  @fixed("yield") class KwYield extends Keyword

  // Symbolic keywords
  @fixed("#") class Hash extends SymbolicKeyword
  @fixed(":") class Colon extends SymbolicKeyword
  @fixed("<%") class Viewbound extends SymbolicKeyword
  @freeform("<-") class LeftArrow extends SymbolicKeyword
  @fixed("<:") class Subtype extends SymbolicKeyword
  @fixed("=") class Equals extends SymbolicKeyword
  @freeform("=>") class RightArrow extends SymbolicKeyword
  @fixed(">:") class Supertype extends SymbolicKeyword
  @fixed("@") class At extends SymbolicKeyword
  @fixed("_") class Underscore extends SymbolicKeyword
  @fixed("=>>") class TypeLambdaArrow extends SymbolicKeyword
  @fixed("?=>") class ContextArrow extends SymbolicKeyword
  @fixed("'") class MacroQuote extends SymbolicKeyword
  @fixed("$") class MacroSplice extends SymbolicKeyword

  // Delimiters
  @fixed("(") class LeftParen extends OpenDelim
  @fixed(")") class RightParen extends CloseDelim
  @fixed(",") class Comma extends Punct
  @fixed(".") class Dot extends Punct
  @fixed(";") class Semicolon extends Punct
  @fixed("[") class LeftBracket extends OpenDelim
  @fixed("]") class RightBracket extends CloseDelim
  @fixed("{") class LeftBrace extends OpenDelim
  @fixed("}") class RightBrace extends CloseDelim

  object Constant {
    @freeform("integer constant") class Int(value: BigInt) extends NumericConstant[BigInt]
    @freeform("long constant") class Long(value: BigInt) extends NumericConstant[BigInt]
    @freeform("float constant") class Float(value: BigDecimal) extends NumericConstant[BigDecimal]
    @freeform("double constant") class Double(value: BigDecimal) extends NumericConstant[BigDecimal]
    @freeform("character constant") class Char(value: scala.Char) extends Constant[scala.Char]
    @freeform("symbol constant") class Symbol(value: scala.Symbol) extends Constant[scala.Symbol]
    @freeform("string constant") class String(value: Predef.String) extends Constant[Predef.String]
  }
  // NOTE: Here's example tokenization of q"${foo}bar".
  // BOF, Id(q)<"q">, Start<"\"">, Part("")<"">, SpliceStart<"$">, {, foo, }, SpliceEnd<"">, Part("bar")<"bar">, End("\""), EOF.
  // As you can see, SpliceEnd is always empty, but I still decided to expose it for consistency reasons.
  object Interpolation {
    @freeform("interpolation id") class Id(value: String) extends Token
    @freeform("interpolation start") class Start extends Token
    @freeform("interpolation part") class Part(value: String) extends Token
    @freeform("splice start") class SpliceStart extends Token
    @freeform("splice end") class SpliceEnd extends Token
    @freeform("interpolation end") class End extends Token
  }
  object Xml {
    @freeform("xml start") class Start extends Token
    @freeform("xml part") class Part(value: String) extends Token
    @freeform("xml splice start") class SpliceStart extends Token
    @freeform("xml splice end") class SpliceEnd extends Token
    @freeform("xml end") class End extends Token
  }

  object Indentation {
    @freeform("indent") class Indent extends Token
    @freeform("outdent") class Outdent extends Token
  }

  // Trivia
  @fixed(" ") class Space extends HSpace
  @fixed("\t") class Tab extends HSpace
  @fixed("\r") class CR extends AtEOL
  @fixed("\n") class LF extends EOL
  @fixed("\f") class FF extends EOL
  @freeform("comment") class Comment(value: String) extends Trivia
  @freeform("beginning of file") class BOF extends Token {
    def this(input: Input, dialect: Dialect) = this(input, dialect, 0)
    def end = start
  }
  @freeform("end of file") class EOF extends Token {
    def this(input: Input, dialect: Dialect) = this(input, dialect, input.chars.length)
    def end = start
  }

  // NOTE: in order to maintain conceptual compatibility with scala.reflect's implementation,
  // Ellipsis.rank = 1 means .., Ellipsis.rank = 2 means ..., etc
  @freeform("\n\n") private[meta] class LFLF extends AtEOL
  @freeform("ellipsis") private[meta] class Ellipsis(rank: Int) extends Token
  @freeform("unquote") private[meta] class Unquote extends Token

  implicit def classifiable[T <: Token]: Classifiable[T] = null
  implicit def showStructure[T <: Token]: Structure[T] = TokenStructure.apply[T]
  implicit def showSyntax[T <: Token](implicit dialect: Dialect): Syntax[T] =
    TokenSyntax.apply[T](dialect)
}
