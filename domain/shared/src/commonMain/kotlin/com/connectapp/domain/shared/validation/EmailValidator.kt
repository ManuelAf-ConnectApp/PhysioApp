package com.connectapp.domain.shared.validation

/**
 * Syntactically valid address with `@` and a domain with a valid suffix,
 * without restricting it to a fixed list of TLDs.
 *
 * Accepted shape (pragmatic subset of RFC 5321/5322, ASCII only):
 * - local part: 1–64 characters of letters, digits and ``!#$%&'*+/=?^_`{|}~-``, with dots
 *   allowed only between characters (no leading, trailing or consecutive dots);
 * - domain: at least two labels of 1–63 letters/digits/hyphens, no label starting or ending with
 *   a hyphen; the last label (TLD) is 2–63 letters or an IDN `xn--` label;
 * - whole address at most 254 characters.
 *
 * PROPUESTA — REQUIERE CONFIRMACIÓN: internationalized addresses (non-ASCII) are rejected, and
 * [normalize] lower-cases the whole address so `Ana@Clinica.es` and `ana@clinica.es` count as the
 * same account (RFC 5321 allows case-sensitive local parts, but no mainstream provider uses them).
 */
object EmailValidator {
    private const val MAX_LENGTH = 254
    private const val MAX_LOCAL_LENGTH = 64
    private const val MAX_LABEL_LENGTH = 63
    private const val LOCAL_SPECIALS = "!#$%&'*+/=?^_`{|}~-"
    private const val IDN_PREFIX = "xn--"

    /** Trimmed, lower-cased form used for storage and uniqueness checks. */
    fun normalize(value: String): String = value.trim().lowercase()

    fun isValid(value: String): Boolean {
        val email = value.trim()
        val at = email.lastIndexOf('@')
        val hasSingleInnerAt = at > 0 && at < email.lastIndex && email.indexOf('@') == at
        return email.length <= MAX_LENGTH && hasSingleInnerAt &&
            isValidLocalPart(email.substring(0, at)) && isValidDomain(email.substring(at + 1))
    }

    private fun isValidLocalPart(local: String): Boolean =
        local.length <= MAX_LOCAL_LENGTH &&
            local.split('.').all { atom ->
                atom.isNotEmpty() && atom.all { it.isAsciiLetterOrDigit() || it in LOCAL_SPECIALS }
            }

    private fun isValidDomain(domain: String): Boolean {
        val labels = domain.split('.')
        if (labels.size < 2) return false
        val labelsValid = labels.all { label ->
            label.length in 1..MAX_LABEL_LENGTH &&
                label.first() != '-' && label.last() != '-' &&
                label.all { it.isAsciiLetterOrDigit() || it == '-' }
        }
        val tld = labels.last()
        val tldValid = tld.length >= 2 &&
            (tld.all { it.isAsciiLetter() } || tld.lowercase().startsWith(IDN_PREFIX))
        return labelsValid && tldValid
    }

    private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
    private fun Char.isAsciiLetterOrDigit() = isAsciiLetter() || this in '0'..'9'
}
