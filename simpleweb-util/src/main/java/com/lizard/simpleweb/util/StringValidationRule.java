package com.lizard.simpleweb.util;

import com.lizard.simpleweb.util.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 描述：
 *
 * @author x
 * @since 2020-09-24 0:42
 */
public class StringValidationRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringValidationRule.class);
    protected List<Pattern> whitelistPatterns = new ArrayList<>();
    protected List<Pattern> blacklistPatterns = new ArrayList<>();
    protected int minLength = 0;
    protected int maxLength = Integer.MAX_VALUE;
    private boolean canonicalizeInput = true;
    private String typeName = null;

    public String getTypeName() {
        return typeName;
    }

    public StringValidationRule(String typeName, String whitelistPattern) {
        this.typeName = typeName;
        addWhitelistPattern(whitelistPattern);
    }

    /**
     * @throws IllegalArgumentException if pattern is null
     */
    public void addWhitelistPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        try {
            whitelistPatterns.add(Pattern.compile(pattern));
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Validation misconfiguration, problem with specified pattern: " + pattern, e);
        }
    }

    /**
     * @throws IllegalArgumentException if p is null
     */
    public void addWhitelistPattern(Pattern p) {
        if (p == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        whitelistPatterns.add(p);
    }

    /**
     * @throws IllegalArgumentException if pattern is null
     */
    public void addBlacklistPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        try {
            blacklistPatterns.add(Pattern.compile(pattern));
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Validation misconfiguration, problem with specified pattern: " + pattern, e);
        }
    }

    /**
     * @throws IllegalArgumentException if p is null
     */
    public void addBlacklistPattern(Pattern p) {
        if (p == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        blacklistPatterns.add(p);
    }

    public void setMinimumLength(int length) {
        minLength = length;
    }


    public void setMaximumLength(int length) {
        maxLength = length;
    }

    public void setCanonicalize(boolean canonicalize) {
        this.canonicalizeInput = canonicalize;
    }

    /**
     * checks input against whitelists.
     *
     * @param context The context to include in exception messages
     * @param input   the input to check
     * @param orig    A origional input to include in exception
     *                messages. This is not included if it is the same as
     *                input.
     * @return input upon a successful check
     */
    private String checkWhitelist(String context, String input, String orig) throws ValidationException {
        // check whitelist patterns
        for (Pattern p : whitelistPatterns) {
            if (!p.matcher(input).matches()) {
                throw new ValidationException(context + ": Invalid input. Please conform to regex " + p.pattern() + (maxLength == Integer.MAX_VALUE ? "" : " with a maximum length of " + maxLength), "Invalid input: context=" + context + ", type(" + getTypeName() + ")=" + p.pattern() + ", input=" + input + (NullSafe.equals(orig, input) ? "" : ", orig=" + orig), context);
            }
        }

        return input;
    }

    /**
     * checks input against whitelists.
     *
     * @param context The context to include in exception messages
     * @param input   the input to check
     * @return input upon a successful check
     * @throws ValidationException if the check fails.
     */
    private String checkWhitelist(String context, String input) throws ValidationException {
        return checkWhitelist(context, input, input);
    }

    /**
     * checks input against blacklists.
     *
     * @param context The context to include in exception messages
     * @param input   the input to check
     * @param orig    A origional input to include in exception
     *                messages. This is not included if it is the same as
     *                input.
     * @return input upon a successful check
     * @throws ValidationException if the check fails.
     */
    private String checkBlacklist(String context, String input, String orig) throws ValidationException {
        // check blacklist patterns
        for (Pattern p : blacklistPatterns) {
            if (p.matcher(input).matches()) {
                throw new ValidationException(context + ": Invalid input. Dangerous input matching " + p.pattern() + " detected.", "Dangerous input: context=" + context + ", type(" + getTypeName() + ")=" + p.pattern() + ", input=" + input + (NullSafe.equals(orig, input) ? "" : ", orig=" + orig), context);
            }
        }

        return input;
    }

    /**
     * checks input against blacklists.
     *
     * @param context The context to include in exception messages
     * @param input   the input to check
     * @return input upon a successful check
     * @throws ValidationException if the check fails.
     */
    private String checkBlacklist(String context, String input) throws ValidationException {
        return checkBlacklist(context, input, input);
    }

    /**
     * checks input lengths
     *
     * @param context The context to include in exception messages
     * @param input   the input to check
     * @param orig    A origional input to include in exception
     *                messages. This is not included if it is the same as
     *                input.
     * @return input upon a successful check
     * @throws ValidationException if the check fails.
     */
    private String checkLength(String context, String input, String orig) throws ValidationException {
        if (input.length() < minLength) {
            throw new ValidationException(context + ": Invalid input. The minimum length of " + minLength + " characters was not met.", "Input does not meet the minimum length of " + minLength + " by " + (minLength - input.length()) + " characters: context=" + context + ", type=" + getTypeName() + "), input=" + input + (NullSafe.equals(input, orig) ? "" : ", orig=" + orig), context);
        }

        if (input.length() > maxLength) {
            throw new ValidationException(context + ": Invalid input. The maximum length of " + maxLength + " characters was exceeded.", "Input exceeds maximum allowed length of " + maxLength + " by " + (input.length() - maxLength) + " characters: context=" + context + ", type=" + getTypeName() + ", orig=" + orig + ", input=" + input, context);
        }

        return input;
    }

    /**
     * checks input lengths
     *
     * @param context The context to include in exception messages
     * @param input   the input to check
     * @return input upon a successful check
     * @throws ValidationException if the check fails.
     */
    private String checkLength(String context, String input) throws ValidationException {
        return checkLength(context, input, input);
    }


    /**
     * {@inheritDoc}
     */
    public String getValid(String context, String input) throws ValidationException {
        String data = null;

        // check for empty/null
        if (checkEmpty(context, input) == null)
            return null;

        // check length
        checkLength(context, input);

        // canonicalize
        if (canonicalizeInput) {
            data = encoder.canonicalize(input);
        } else {
            String message = String.format("Input validaiton excludes canonicalization.  Context: %s   Input: %s", context, input);
            LOGGER.warning(Logger.SECURITY_AUDIT, message);
            data = input;
        }

        // check whitelist patterns
        checkWhitelist(context, input);

        // check blacklist patterns
        checkBlacklist(context, input);

        // validation passed
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public String sanitize(String context, String input) {
        return whitelist(input, EncoderConstants.CHAR_ALPHANUMERICS);
    }

}