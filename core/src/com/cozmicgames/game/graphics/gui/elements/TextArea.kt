package com.cozmicgames.game.graphics.gui.elements

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils
import com.badlogic.gdx.utils.Align
import com.cozmicgames.game.*
import com.cozmicgames.common.utils.collections.FixedSizeStack
import com.cozmicgames.common.utils.collections.Pool
import com.cozmicgames.game.graphics.engine.graphics2d.TextRenderable2D
import com.cozmicgames.game.graphics.gui.DefaultStyle
import com.cozmicgames.game.graphics.gui.GUIElement
import com.cozmicgames.game.graphics.gui.skin.GUIElementStyle
import com.cozmicgames.game.graphics.gui.skin.GUISkin
import com.cozmicgames.game.graphics.gui.skin.boolean
import com.cozmicgames.game.graphics.gui.skin.color
import com.cozmicgames.game.graphics.gui.skin.drawable
import com.cozmicgames.game.graphics.gui.skin.font
import com.cozmicgames.game.graphics.gui.skin.int
import com.cozmicgames.game.graphics.gui.skin.optionalDrawable
import com.cozmicgames.game.input.InputListener
import com.cozmicgames.common.utils.extensions.sum
import engine.input.GestureListener
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class TextArea(text: String, val style: TextAreaStyle = TextAreaStyle()) : GUIElement() {
    constructor(skin: GUISkin, name: String = "default", text: String) : this(text, skin.getStyle(TextAreaStyle::class, name)!!)

    private companion object {
        const val UNDO_REDO_STACK_SIZE = 100

        var showCursor = false

        init {
            Game.tasks.schedule(0.5f, true) {
                showCursor = !showCursor
            }
        }
    }

    class TextAreaStyle : GUIElementStyle() {
        var textColor by color { it.color.set(DefaultStyle.normalTextColor) }
        var textColorDisabled by color { it.color.set(DefaultStyle.normalTextColor) }
        var font by font {}
        var align by int { it.value = Align.center }
        var wrap by boolean { it.value = true }
        var isFixedTextSize by boolean { it.value = true }
        var background by optionalDrawable { DefaultStyle.normalDrawable() }
        var backgroundDisabled by optionalDrawable { DefaultStyle.disabledDrawable() }
        var cursor by drawable { DefaultStyle.textCursorDrawable() }
        var selection by drawable { DefaultStyle.textSelectionDrawable() }
    }

    interface TextFormatter {
        fun getColor(word: String): Color?
    }

    object DefaultTextFormatter : TextFormatter {
        override fun getColor(word: String) = null
    }

    private enum class CursorMode {
        SINGLE,
        SELECTION
    }

    private inner class Cursor() {
        var lineIndex = 0
        var indexInLine = 0

        val textIndex get() = getTextIndex(lineIndex, indexInLine)

        fun set(cursor: Cursor): Cursor {
            lineIndex = cursor.lineIndex
            indexInLine = cursor.indexInLine
            return this
        }
    }

    private class Line {
        var lineIndex = 0
        var y = 0.0f
        var x = 0.0f
        var count = 0
        var totalWidth = 0.0f
        val widths = arrayListOf<Float>()

        fun set(lineIndex: Int, x: Float, y: Float, run: GlyphRun, hasNextLine: Boolean) {
            this.lineIndex = lineIndex
            this.x = x
            this.y = y
            widths.clear()
            repeat(run.xAdvances.size - 1) {
                widths += run.xAdvances[it + 1]
            }
            count = if (hasNextLine) widths.size + 1 else widths.size
            totalWidth = widths.sum().toFloat()
        }
    }

    override val usedLayers = 3

    var textFormatter: TextFormatter = DefaultTextFormatter

    open var onEnter: (() -> Unit)? = null
    open var replacementChar: Char? = null
        set(value) {
            field = value
            updateDisplayText()
        }

    open val supportsEditing = true
    open val supportsMultiline = true
    open val allowSelection = true
    open val allowUndoRedo = true

    var text
        set(value) {
            internalText = value

            if (currentCursorMode == CursorMode.SELECTION) {
                if (cursor1.textIndex < cursor0.textIndex)
                    cursor0.set(cursor1)

                currentCursorMode = CursorMode.SINGLE
            }

            while (cursor0.textIndex >= value.length) {
                if (cursor0.indexInLine == 0) {
                    if (cursor0.lineIndex == 0)
                        break

                    cursor0.lineIndex--
                    cursor0.indexInLine = lines.getOrNull(cursor0.lineIndex)?.count ?: 0

                    if (cursor0.textIndex == value.length)
                        break
                }

                cursor0.indexInLine--
            }
        }
        get() = internalText

    private var internalText = text
        set(value) {
            field = value
            updateDisplayText()
        }

    private var displayText = text
    private var currentCursorMode = CursorMode.SINGLE
    private var isFocused = false
    private var drawable = style.background
    private var textColor = style.textColor
    private val cursor0 = Cursor()
    private val cursor1 = Cursor()
    private val lines = arrayListOf<Line>()
    private val linePool = Pool(supplier = { Line() })
    private val pendingChars = arrayListOf<Char>()
    private var isDoubleTapped = false
    private val undoStack = FixedSizeStack<String>(UNDO_REDO_STACK_SIZE)
    private val redoStack = FixedSizeStack<String>(UNDO_REDO_STACK_SIZE)

    private val inputListener = object : InputListener {
        override fun onChar(char: Char) {
            if (!isFocused || gui?.isInteractionEnabled == false)
                return

            if (char == '\t')
                return

            if (char == '\n') {
                if (!supportsMultiline)
                    return
                else
                    pendingChars += char
            } else
                if (Game.fonts.getFont(style.font.font).isDisplayable(char))
                    pendingChars += char
        }
    }

    private val gestureListener = object : GestureListener {
        override fun onTap(x: Float, y: Float, count: Int) {
            if (!isFocused)
                return

            isDoubleTapped = count > 1
        }
    }

    init {
        addListener(object : Listener {
            override fun onEnable(element: GUIElement) {
                if (isHovered)
                    Gdx.graphics.setSystemCursor(SystemCursor.Ibeam)

                drawable = style.background
                textColor = style.textColor
            }

            override fun onDisable(element: GUIElement) {
                Gdx.graphics.setSystemCursor(SystemCursor.Arrow)

                drawable = style.backgroundDisabled
                textColor = style.textColorDisabled
            }

            override fun onEnter(element: GUIElement) {
                if (isEnabled)
                    Gdx.graphics.setSystemCursor(SystemCursor.Ibeam)
            }

            override fun onExit(element: GUIElement) {
                Gdx.graphics.setSystemCursor(SystemCursor.Arrow)
            }

            override fun onUpdate(element: GUIElement, delta: Float, scissorRectangle: Rectangle?) {
                if (Game.input.justTouchedDown) {
                    isFocused = if (isHovered) {
                        Gdx.input.setOnscreenKeyboardVisible(true)
                        true
                    } else {
                        Gdx.input.setOnscreenKeyboardVisible(false)
                        false
                    }
                }
            }
        })

        updateDisplayText()
    }

    fun unfocus() {
        isFocused = false
    }

    private fun setContent(text: String) {
        undoStack.push(this.text)
        this.internalText = text
        redoStack.clear()
    }

    private fun getTextIndex(lineIndex: Int, indexInLine: Int): Int {
        var index = indexInLine
        repeat(lineIndex) {
            index += lines[it].count
        }
        return index
    }

    private fun updateDisplayText() {
        replacementChar?.let {
            displayText = String(CharArray(text.length).apply { fill(it) })
            return
        }

        val wordBuilder = StringBuilder()
        val displayTextBuilder = StringBuilder()

        text.forEach {
            if (it.isLetter())
                wordBuilder.append(it)
            else {
                if (wordBuilder.isNotEmpty()) {
                    val word = wordBuilder.toString()
                    wordBuilder.clear()
                    val color = textFormatter.getColor(word) ?: textColor.color
                    displayTextBuilder.append("[#$color]")
                    displayTextBuilder.append(word)
                }

                displayTextBuilder.append(it)
            }
        }

        if (wordBuilder.isNotEmpty())
            displayTextBuilder.append(wordBuilder.toString())

        displayText = displayTextBuilder.toString()
    }

    private fun undo() {
        if (undoStack.isEmpty)
            return

        val text = undoStack.pop()
        redoStack.push(this.text)
        this.text = text
    }

    private fun redo() {
        if (redoStack.isEmpty)
            return

        val text = redoStack.pop()
        undoStack.push(this.text)
        this.text = text
    }

    private fun addText(text: String) {
        if (text.isEmpty())
            return

        if (currentCursorMode == CursorMode.SELECTION) {
            if (cursor1.textIndex < cursor0.textIndex)
                cursor0.set(cursor1)

            currentCursorMode = CursorMode.SINGLE
        }

        when (val index = cursor0.textIndex) {
            0 -> setContent(text + this.text)
            this.text.length -> setContent(this.text + text)
            else -> setContent(this.text.substring(0, index) + text + this.text.substring(index, this.text.length))
        }

        if (text.contains('\n')) {
            Gdx.app.postRunnable {
                val newLineCount = text.count { it == '\n' }
                cursor0.lineIndex += newLineCount
                cursor0.indexInLine = text.length - newLineCount
            }
        } else
            cursor0.indexInLine += text.length
    }

    private fun removeCharBeforeCursor() {
        require(currentCursorMode == CursorMode.SINGLE)

        val index = cursor0.textIndex

        if (index > 0) {
            setContent(if (index < text.length)
                text.substring(0, index - 1) + text.substring(index, text.length)
            else
                text.substring(0, text.length - 1))

            if (cursor0.indexInLine == 0) {
                if (cursor0.lineIndex > 0) {
                    cursor0.lineIndex--
                    cursor0.indexInLine = lines[cursor0.lineIndex].count - 1
                }
            } else
                cursor0.indexInLine--
        }
    }

    private fun removeCharAfterCursor() {
        require(currentCursorMode == CursorMode.SINGLE)

        val index = cursor0.textIndex

        if (index < text.length) {
            if (index > 0)
                setContent(text.substring(0, index) + text.substring(index + 1, text.length))
            else if (text.length > 1)
                setContent(text.substring(1, text.length))
            else if (text.length == 1)
                setContent("")
        }
    }

    private fun removeSelection(replacement: String = ""): String {
        if (currentCursorMode == CursorMode.SINGLE)
            return ""

        var cursor0 = cursor0
        var cursor1 = cursor1

        if (cursor1.textIndex < cursor0.textIndex) {
            val temp = cursor0
            cursor0 = cursor1
            cursor1 = temp
        }

        val index0 = cursor0.textIndex
        val index1 = cursor1.textIndex
        val selection = text.substring(index0, index1)

        if (replacement.isNotEmpty()) {
            text = text.substring(0, index0) + text.substring(index1, text.length)
            addText(replacement)
        } else
            setContent(text.substring(0, index0) + text.substring(index1, text.length))

        this.cursor0.set(cursor0)
        currentCursorMode = CursorMode.SINGLE
        return selection
    }

    private fun getSelection(): String {
        if (currentCursorMode != CursorMode.SELECTION)
            return ""

        if (cursor1.textIndex < cursor0.textIndex) {
            val temp = Cursor().set(cursor0)
            cursor0.set(cursor1)
            cursor1.set(temp)
        }

        return text.substring(cursor0.textIndex, cursor1.textIndex)
    }

    private fun moveCursor(cursor: Cursor, x: Int, y: Int) {
        if (x > 0) {
            var xx = x
            while (xx > 0) {
                if (cursor.indexInLine + 1 > lines[cursor.lineIndex].count) {
                    if (cursor.lineIndex < lines.lastIndex) {
                        cursor.lineIndex++
                        cursor.indexInLine = 0
                    } else
                        break
                } else
                    cursor.indexInLine++
                xx--
            }
        } else if (x < 0) {
            var xx = x
            while (xx < 0) {
                if (cursor.indexInLine <= 0) {
                    if (cursor.lineIndex > 0) {
                        cursor.lineIndex--
                        cursor.indexInLine = lines[cursor.lineIndex].count - 1
                    } else
                        break
                } else
                    cursor.indexInLine--
                xx++
            }
        }

        if (y != 0) {
            val previousLineIndex = cursor.lineIndex

            cursor.lineIndex = if (y > 0 && cursor.lineIndex < lines.lastIndex)
                min(lines.lastIndex, cursor.lineIndex + y)
            else
                max(0, cursor.lineIndex + y)

            val previousLine = lines[previousLineIndex]
            val cursorX = if (cursor.indexInLine < previousLine.count)
                previousLine.x + previousLine.widths.sum(cursor.indexInLine).toFloat()
            else
                previousLine.x + previousLine.totalWidth

            val cursorLine = lines[cursor.lineIndex]
            var checkX = cursorLine.x
            var bestIndex = 0
            var bestDifference = Float.MAX_VALUE

            cursorLine.widths.forEachIndexed { index, width ->
                val difference = abs(abs(checkX) - abs(cursorX))
                if (difference < bestDifference) {
                    bestIndex = index
                    bestDifference = difference
                }
                checkX += width
            }

            cursor.indexInLine = bestIndex
        }
    }

    private fun getWordMoveDistance(cursor: Cursor, increment: Int): Int {
        if (text.isEmpty() || increment == 0)
            return 0

        val isWhitespace = text[when (cursor.textIndex) {
            text.length -> cursor.textIndex - 1
            else -> cursor.textIndex
        }].isWhitespace()

        fun isValid(index: Int): Boolean {
            if (index == 0)
                return increment > 0

            if (index == text.length)
                return increment < 0

            return if (isWhitespace)
                text[index].isWhitespace()
            else
                !text[index].isWhitespace()
        }

        var distance = 0
        while (isValid(cursor.textIndex + distance))
            distance += increment
        return distance
    }

    private fun moveCursors(x: Int, y: Int, moveWord: Boolean) {
        if (text.isEmpty())
            return

        if (currentCursorMode == CursorMode.SELECTION)
            currentCursorMode = CursorMode.SINGLE

        moveCursor(cursor0, if (moveWord) getWordMoveDistance(cursor0, x) else x, y)
    }

    private fun moveSelection(x: Int, y: Int, moveWord: Boolean) {
        if (text.isEmpty())
            return

        if (currentCursorMode == CursorMode.SINGLE) {
            cursor1.set(cursor0)
            currentCursorMode = CursorMode.SELECTION
        }

        val xDistance = if (moveWord)
            getWordMoveDistance(cursor1, x)
        else
            x
        moveCursor(cursor1, xDistance, y)
        if (cursor0.textIndex == cursor1.textIndex)
            currentCursorMode = CursorMode.SINGLE
    }

    private fun updateCursors(layout: GlyphLayout, textX: Float, textY: Float) {
        if (!isFocused)
            return

        val gui = gui ?: return

        lines.forEach {
            linePool.free(it)
        }
        lines.clear()

        val lineHeight = layout.height / layout.runs.size
        layout.runs.forEachIndexed { index, run ->
            val x = run.x + textX
            val y = run.y + textY
            val line = linePool.obtain()
            line.set(index, x, y, run, index < layout.runs.size - 1)
            lines += line
        }

        fun getLineIndexFromPosition(y: Float = gui.inputY): Int {
            if (lines.isEmpty())
                return 0

            if (y < lines.first().y)
                return 0

            lines.forEachIndexed { index, line ->
                if (y >= line.y && y < line.y + lineHeight)
                    return index
            }

            return lines.lastIndex
        }

        fun getCursorIndexFromPosition(lineIndex: Int, x: Float = gui.inputX): Int {
            if (lines.isEmpty())
                return 0

            val line = lines[lineIndex]

            if (x < line.x)
                return 0

            var checkX = line.x
            line.widths.forEachIndexed { index, width ->
                if (x >= checkX && x < checkX + width)
                    return index

                checkX += width
            }

            return line.count
        }

        val isShift = UIUtils.shift()
        val isControl = UIUtils.ctrl()

        if (Game.input.justTouchedDown) {
            currentCursorMode = CursorMode.SINGLE
            cursor0.lineIndex = getLineIndexFromPosition()
            cursor0.indexInLine = getCursorIndexFromPosition(cursor0.lineIndex)
        }

        if (allowSelection && Game.input.isTouched) {
            val lineIndex = getLineIndexFromPosition()
            val indexInLine = getCursorIndexFromPosition(lineIndex)

            if (lineIndex == cursor0.lineIndex && indexInLine == cursor0.indexInLine) {
                cursor0.lineIndex = lineIndex
                cursor0.indexInLine = indexInLine
                currentCursorMode = CursorMode.SINGLE
            } else {
                cursor1.lineIndex = lineIndex
                cursor1.indexInLine = indexInLine
                currentCursorMode = CursorMode.SELECTION
            }
        }

        if (allowSelection && isDoubleTapped && text.isNotEmpty()) {
            val lineIndex = getLineIndexFromPosition()
            val indexInLine = getCursorIndexFromPosition(lineIndex)

            cursor0.lineIndex = lineIndex
            cursor1.lineIndex = lineIndex

            when (indexInLine) {
                0 -> {
                    cursor0.indexInLine = 0
                    cursor1.indexInLine = 1

                    while (cursor1.textIndex < lines[lineIndex].count && !text[cursor1.textIndex].isWhitespace())
                        cursor1.indexInLine++
                }

                lines[lineIndex].count -> {
                    cursor0.indexInLine = indexInLine - 1
                    cursor1.indexInLine = indexInLine

                    while (cursor0.textIndex > 0 && !text[cursor0.textIndex - 1].isWhitespace())
                        cursor0.indexInLine--
                }

                else -> {
                    cursor0.indexInLine = indexInLine - 1
                    cursor1.indexInLine = indexInLine

                    while (cursor0.textIndex > 0 && !text[cursor0.textIndex - 1].isWhitespace())
                        cursor0.indexInLine--

                    while (cursor1.textIndex < lines[lineIndex].count && !text[cursor1.textIndex].isWhitespace())
                        cursor1.indexInLine++
                }
            }

            currentCursorMode = CursorMode.SELECTION
            isDoubleTapped = false
        }

        if (Game.input.isKeyJustDown(Keys.LEFT))
            if (isShift) moveSelection(-1, 0, isControl) else moveCursors(-1, 0, isControl)

        if (Game.input.isKeyJustDown(Keys.RIGHT))
            if (isShift) moveSelection(1, 0, isControl) else moveCursors(1, 0, isControl)

        if (Game.input.isKeyJustDown(Keys.UP))
            if (isShift) moveSelection(0, -1, isControl) else moveCursors(0, -1, false)

        if (Game.input.isKeyJustDown(Keys.DOWN))
            if (isShift) moveSelection(0, 1, isControl) else moveCursors(0, 1, false)

        if (isControl && Game.input.isKeyJustDown(Keys.C))
            Gdx.app.clipboard.contents = getSelection()

        if (isControl && Game.input.isKeyJustDown(Keys.A)) {
            currentCursorMode = CursorMode.SELECTION
            cursor0.lineIndex = 0
            cursor0.indexInLine = 0
            cursor1.lineIndex = lines.lastIndex
            cursor1.indexInLine = lines.lastOrNull()?.count ?: 0
        }

        if (!supportsEditing)
            return

        if (allowUndoRedo) {
            if (isControl && Game.input.isKeyJustDown(Keys.Z))
                undo()

            if (isControl && Game.input.isKeyJustDown(Keys.Y))
                redo()
        }

        if (Game.input.isKeyJustDown(Keys.BACKSPACE))
            if (currentCursorMode == CursorMode.SELECTION) removeSelection() else removeCharBeforeCursor()

        if (Game.input.isKeyJustDown(Keys.FORWARD_DEL))
            if (currentCursorMode == CursorMode.SELECTION) removeSelection() else removeCharAfterCursor()

        if (isControl && Game.input.isKeyJustDown(Keys.V)) {
            if (Gdx.app.clipboard.hasContents()) {
                when (currentCursorMode) {
                    CursorMode.SINGLE -> addText(Gdx.app.clipboard.contents)
                    CursorMode.SELECTION -> removeSelection(Gdx.app.clipboard.contents)
                }
            }
        }

        if (isControl && Game.input.isKeyJustDown(Keys.X))
            Gdx.app.clipboard.contents = removeSelection()

        if (Game.input.isKeyJustDown(Keys.ENTER))
            onEnter?.invoke()

        val pendingText = pendingChars.joinToString("")
        if (pendingText.isNotEmpty())
            when (currentCursorMode) {
                CursorMode.SINGLE -> addText(pendingText)
                CursorMode.SELECTION -> {
                    removeSelection(pendingText)
                    currentCursorMode = CursorMode.SINGLE
                }
            }
        pendingChars.clear()
    }

    override fun onAdded() {
        if (!supportsEditing)
            return

        Game.input.addListener(inputListener)
        Game.gestures.addListener(gestureListener)
    }

    override fun onRemoved() {
        if (!supportsEditing)
            return

        Game.input.removeListener(inputListener)
        Game.gestures.removeListener(gestureListener)
    }

    override fun render() {
        val paddingTop = drawable?.paddingTop ?: 0.0f
        val paddingLeft = drawable?.paddingLeft ?: 0.0f
        val paddingRight = drawable?.paddingRight ?: 0.0f
        val paddingBottom = drawable?.paddingBottom ?: 0.0f

        val textSize = if (style.isFixedTextSize.value)
            gui!!.textSize
        else
            height - paddingTop - paddingBottom

        drawable?.let {
            it.drawable.draw(layer, it.color, x, y, width, height)
        }

        var textX = 0.0f
        var textY = 0.0f

        Game.graphics2d.submit<TextRenderable2D> {
            it.layer = layer + 1
            it.font = style.font.font
            it.style = style.font.fontStyle
            it.color = textColor.color
            it.text = displayText
            it.size = textSize
            it.wrap = style.wrap.value
            if (style.wrap.value)
                it.targetWidth = width - paddingLeft - paddingRight
            textX = x + paddingLeft + when {
                Align.isCenterHorizontal(style.align.value) -> (width - paddingLeft - paddingRight - it.layout.width) * 0.5f
                Align.isRight(style.align.value) -> width - paddingLeft - paddingRight - it.layout.width
                else -> paddingLeft
            }
            it.x = textX
            textY = y + paddingTop + when {
                Align.isCenterVertical(style.align.value) -> (height - paddingTop - paddingBottom - it.layout.height) * 0.5f
                Align.isBottom(style.align.value) -> height - paddingTop - paddingBottom - it.layout.height
                else -> paddingTop
            }
            it.y = textY
            it.layout.let { layout ->
                minContentWidth = layout.width + paddingLeft + paddingRight
                minContentHeight = layout.height + paddingTop + paddingBottom
            }
            updateCursors(it.layout, textX, textY)
        }

        if (isFocused) {
            when (currentCursorMode) {
                CursorMode.SINGLE -> {
                    if (showCursor) {
                        var cursorX = textX
                        var cursorY = textY

                        if (text.isNotEmpty()) {
                            val cursorLine = lines.getOrNull(cursor0.lineIndex) ?: return

                            cursorY = cursorLine.y
                            cursorX = if (cursor0.indexInLine < cursorLine.count)
                                cursorLine.x + cursorLine.widths.sum(cursor0.indexInLine).toFloat()
                            else
                                cursorLine.x + cursorLine.totalWidth
                        }

                        style.cursor.drawable.draw(layer + 2, style.cursor.color, cursorX, cursorY, 1.0f, textSize)
                    }
                }

                CursorMode.SELECTION -> {
                    var cursor0 = cursor0
                    var cursor1 = cursor1

                    if (cursor1.textIndex < cursor0.textIndex) {
                        val temp = cursor0
                        cursor0 = cursor1
                        cursor1 = temp
                    }

                    val lineIndex0 = cursor0.lineIndex
                    val lineIndex1 = cursor1.lineIndex

                    if (lineIndex0 == lineIndex1) {
                        val line = lines[lineIndex0]

                        val selectionY = line.y
                        val selectionX0 = if (cursor0.indexInLine < line.count)
                            line.x + line.widths.sum(cursor0.indexInLine).toFloat()
                        else
                            line.x + line.totalWidth

                        val selectionX1 = if (cursor1.indexInLine < line.count)
                            line.x + line.widths.sum(cursor1.indexInLine).toFloat()
                        else
                            line.x + line.totalWidth

                        val selectionWidth = selectionX1 - selectionX0

                        style.selection.drawable.draw(layer + 2, style.selection.color, selectionX0, selectionY, selectionWidth, textSize)
                    } else
                        (lineIndex0..lineIndex1).forEach {
                            val line = lines[it]

                            val selectionY = line.y

                            val selectionX0 = when (it) {
                                lineIndex0 -> if (cursor0.indexInLine < line.count)
                                    line.x + line.widths.sum(cursor0.indexInLine).toFloat()
                                else
                                    line.x + line.totalWidth

                                else -> line.x
                            }

                            val selectionX1 = when (it) {
                                lineIndex1 -> if (cursor1.indexInLine < line.count)
                                    line.x + line.widths.sum(cursor1.indexInLine).toFloat()
                                else
                                    line.x + line.totalWidth

                                else -> line.x + line.totalWidth
                            }

                            val selectionWidth = selectionX1 - selectionX0
                            style.selection.drawable.draw(layer + 2, style.selection.color, selectionX0, selectionY, selectionWidth, textSize)
                        }
                }
            }
        }
    }
}
