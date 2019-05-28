package com.github.rwsbillyang.appbase.util

import android.graphics.Color
import android.graphics.MaskFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.view.View

/**
 *
//usage1:
span {
bold { +context.getString(R.string.author) }
+" "
if (authorUrl != null) url(authorUrl) { +author } else +author
}
//usage2:
myTextView.apply {
text = span {
font("serif"){+"I am font"}
ln()
bold{ + "bold text"}
ln()
italic{ +"italic" }
ln()
boldItalic{ +"boldItalic" }
ln()
absoluteSize(30){ +"30sp font Size"}
ln()
relativeSize(1.5F){ +"1.5 times font Size"}
ln()
xScale(2.0F){+"xScale with 2" }
ln()
strike{+"strike me"}
ln()
underline{+"underline me"}
ln()
+"Y";superscript{+"2"}
ln()
+"X";subscript{+"2"}
ln()
backgroundColor(Color.RED){+"backgroundColor with red"}
ln()
foregroundColor(Color.BLUE){+"foregroundColor with BLUE"}
ln()
maskFilter(BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)) { +"blur text" }
ln()
clickable({toast("you clicked me")}){ +"clickme" }
ln()
url("tel:18610177823"){ +"contact me" }
ln()
+ "drawale:"
drawale(getResources().getDrawable(R.drawable.ic_volume_up_black_24dp)){+"drawale"}
ln()
bullet{+ "bullet1"}
ln()
bullet{+ "bullet2"}
ln()
relativeSize(2.0F){foregroundColor(Color.RED){+"惊爆价：￥99"}}
+"原价："
strike{relativeSize(0.8F){foregroundColor(Color.GRAY){+"￥399"}}}
}
movementMethod = LinkMovementMethod.getInstance()
}
 * */
fun span(init: SpanOp): Spannable {
    val spanWithChildren = Span()
    spanWithChildren.init()
    return spanWithChildren.build()
}

typealias SpanOp = Span.() -> Unit

@Suppress("unused")
open class Span {

    private var spans = emptyList<Span>()

    open fun build(builder: SpannableStringBuilder = SpannableStringBuilder()): Spannable {
        spans.forEach { it.build(builder) }
        return builder
    }


    class Node(val span: Any) : Span() {
        override fun build(builder: SpannableStringBuilder): Spannable {
            val start = builder.length
            super.build(builder)
            builder.setSpan(span, start, builder.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return builder
        }
    }

    class Leaf(val content: Any) : Span() {
        override fun build(builder: SpannableStringBuilder): Spannable {
            builder.append(content.toString())
            return builder
        }
    }
    /**
     * addNode将添加一个node，也就是对大括号中的文本（leaf）执行某个span动作，即对大括号中的内容添加格式
     * */
    fun addNode(what: Any, init: Node.() -> Unit) {
        spans += Node(what).apply(init)
    }

    /**
     * 添加一个plain字符串,即大括号{ +"xxx" }里面的代码将添加一个Leaf
     * */
    operator fun String.unaryPlus() {
        spans += Leaf(this)
    }

    fun ln() {
        +"\n"
    }
    /**
     * 设置字体 setFont  default,default-bold,monospace,serif,sans-serif
     * */
    fun font(font: String, init: Span.() -> Unit) {
        addNode(TypefaceSpan(font), init)
    }

    fun normal(init: Span.() -> Unit){
        addNode(StyleSpan(Typeface.NORMAL), init)
    }
    /**
     * 默认添加一个bold StyleSpan，也可以是其它StyleSpan，并执行后面大括号中的代码块
     * */
    fun bold(init: Span.() -> Unit) {
        addNode(StyleSpan(Typeface.BOLD), init)
    }
    fun italic(init: Span.() -> Unit) {
        addNode( StyleSpan(Typeface.ITALIC), init)
    }
    fun boldItalic(init: Span.() -> Unit) {
        addNode(StyleSpan(Typeface.BOLD_ITALIC), init)
    }

    fun absoluteSize(size: Int, init: Span.() -> Unit ){
        addNode(AbsoluteSizeSpan(size),init)
    }

    fun relativeSize(proportion: Float, init: Span.() -> Unit){
        addNode(RelativeSizeSpan(proportion),init)
    }

    fun xScale(proportion: Float, init: Span.() -> Unit){
        addNode(ScaleXSpan(proportion),init)
    }


    fun strike(init: Span.() -> Unit) {
        addNode(StrikethroughSpan(), init)
    }
    fun underline(init: Span.() -> Unit){
        addNode(UnderlineSpan(),init)
    }

    fun superscript(init: Span.() -> Unit){
        addNode(SuperscriptSpan(),init)
    }
    fun subscript(init: Span.() -> Unit){
        addNode(SubscriptSpan(),init)
    }

    /**
     * 为文本设置背景色，相当于TextView的setBackground()
     * */
    fun backgroundColor(color: Int, init: Span.() -> Unit){
        addNode(BackgroundColorSpan(color),init)
    }
    /**
     * ForegroundColorSpan为文本设置前景色，相当于TextView的setTextColor()
     * */
    fun color(color: Int, init: Span.() -> Unit){
        addNode(ForegroundColorSpan(color),init)
    }
    /**
     * val blurMask: MaskFilter =  BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL);
     * maskFilter(BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)) { +"blur text" }
     * */
    fun maskFilter(filter: MaskFilter, init: Span.() -> Unit){
        addNode(MaskFilterSpan(filter),init)
    }

//    fun rasterizer(init: Span.() -> Unit){
//        addNode(RasterizerSpan(),init)
//    }

    fun bullet(color: Int = Color.BLACK, init: Span.() -> Unit){
        addNode(BulletSpan(android.text.style.BulletSpan.STANDARD_GAP_WIDTH,color),init)
    }


    /**
     * 添加一个click点击事件，并执行后面大括号中的代码块
     * */
    fun clickable(onClick: () -> Unit, init: Span.() -> Unit) {
        val span = object : ClickableSpan() {
            override fun onClick(view: View?) {
                onClick()
            }
        }
        addNode(span, init)
    }

    /**
     * 添加一个URLSpan，并执行后面大括号中的代码块
     * 支持：http://www.baidu.com     mailto:webmaster@google.com
     * tel:4155551212     sms:4155551212    mms:4155551212
     * geo:38.899533,-77.036476
     * */
    fun url(url: String, init: Span.() -> Unit) {
        addNode(URLSpan(url), init)
    }

    fun drawable(drawable: Drawable,align:Int= ImageSpan.ALIGN_BOTTOM,init: Span.() -> Unit)
    {
        //DrawableMarginSpan IconMarginSpan
        addNode( ImageSpan(drawable
            .apply { setBounds(0, 0, getIntrinsicWidth(), getIntrinsicHeight()) },align), init)
    }


    /**
     * 简单地解析单个标签
     * @param html, html text
     * @param tag, such as p, ,span, div etc.
     * @param tagSpan 标签内的文本格式，
     * @param plainSpan 标签外的文本格式
     * <pre>
     *     val html = "blabla <p>blaxxxx</p>  yyyyyyy  <p>xxxx</p>  ssss"
     *
     *     parseHtml(html, "b", { str -> italic {color(fontColor){ +str } } },{str -> normal { +str }})
     * </pre>
     * */
    fun parseHtmlWithSingleTag(html: String, tag:String, tagSpan: Span.(String) -> Unit, plainSpan:Span.(String) -> Unit){
        val start = "<$tag>"
        val startIndex = html.indexOf(start,0,true)

        if(startIndex<0) {
            plainSpan(html)
            //+html
        }else{
            plainSpan(html.substring(0, startIndex))
            val end:String = "</$tag>"
            val endIndex = html.indexOf(end,startIndex+start.length,true)

            tagSpan(html.substring(startIndex+start.length, endIndex))

            if(endIndex + end.length < html.length){
                parseHtmlWithSingleTag(html.substring(endIndex + end.length, html.length),tag,tagSpan,plainSpan)
            }
        }
    }
}



