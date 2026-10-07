package com.eirahoutmoss.nextone.core

/**
 * Profil dosyaları için küçük, bağımlılıksız JSON okuyucu.
 * Dönüş tipleri: Map<String, Any?>, List<Any?>, String, Double, Boolean, null.
 */
object Json {
    fun parse(text: String): Any? {
        val p = Parser(text)
        p.ws()
        val v = p.value()
        p.ws()
        if (p.i != text.length) p.fail("Fazladan içerik")
        return v
    }

    private class Parser(val s: String) {
        var i = 0

        fun fail(msg: String): Nothing {
            val line = s.substring(0, minOf(i, s.length)).count { it == '\n' } + 1
            throw IllegalArgumentException("JSON hatası ($line. satır): $msg")
        }

        fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }

        fun value(): Any? {
            if (i >= s.length) fail("Beklenmeyen dosya sonu")
            return when (val c = s[i]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> lit("true", true)
                'f' -> lit("false", false)
                'n' -> lit("null", null)
                else -> if (c == '-' || c.isDigit()) num() else fail("Beklenmeyen karakter '$c'")
            }
        }

        fun lit(word: String, v: Any?): Any? {
            if (!s.startsWith(word, i)) fail("'$word' bekleniyordu")
            i += word.length
            return v
        }

        fun obj(): Map<String, Any?> {
            val m = LinkedHashMap<String, Any?>()
            i++; ws()
            if (s.getOrNull(i) == '}') { i++; return m }
            while (true) {
                ws()
                if (s.getOrNull(i) != '"') fail("Anahtar bekleniyordu")
                val k = str()
                ws()
                if (s.getOrNull(i) != ':') fail("':' bekleniyordu")
                i++; ws()
                m[k] = value()
                ws()
                when (s.getOrNull(i)) {
                    ',' -> i++
                    '}' -> { i++; return m }
                    else -> fail("',' veya '}' bekleniyordu")
                }
            }
        }

        fun arr(): List<Any?> {
            val l = ArrayList<Any?>()
            i++; ws()
            if (s.getOrNull(i) == ']') { i++; return l }
            while (true) {
                ws()
                l += value()
                ws()
                when (s.getOrNull(i)) {
                    ',' -> i++
                    ']' -> { i++; return l }
                    else -> fail("',' veya ']' bekleniyordu")
                }
            }
        }

        fun str(): String {
            i++
            val sb = StringBuilder()
            while (true) {
                if (i >= s.length) fail("Kapanmamış metin")
                val c = s[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        when (val e = s.getOrNull(i++)) {
                            '"' -> sb.append('"'); '\\' -> sb.append('\\'); '/' -> sb.append('/')
                            'b' -> sb.append('\b'); 'f' -> sb.append('\u000C'); 'n' -> sb.append('\n')
                            'r' -> sb.append('\r'); 't' -> sb.append('\t')
                            'u' -> {
                                if (i + 4 > s.length) fail("Eksik \\u kaçışı")
                                sb.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4
                            }
                            else -> fail("Geçersiz kaçış '\\$e'")
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        fun num(): Double {
            val start = i
            if (s[i] == '-') i++
            while (i < s.length && (s[i].isDigit() || s[i] in ".eE+-")) i++
            return s.substring(start, i).toDoubleOrNull() ?: fail("Geçersiz sayı")
        }
    }
}
