package com.lizard.simpleweb.util;

import com.lizard.simpleweb.util.exception.ValidationException;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 描述：
 *
 * @author x
 * @since 2020-09-23 23:40
 */
public class SecurityValidator {
    /**
     * 重定向Url校验
     * 1.白名单
     * 2.以"/"开头的重定向到本地路径不安全
     * 3.
     * <p>
     * >Input starting with a / to redirect to local pages is not safe. //example.org is a valid URL.
     * Input starting with the desired domain name is not safe.
     * https://example.org.attacker.com is valid.
     * Only allow HTTP(S) protocols. All other protocols,
     * including JavaScript URIs such as javascript:alert(1) should be blocked Data URIs such as
     * data:text/html,<script>alert(document.domain)</script> should be blocked
     * URIs containing CRLF characters can lead to header injection or response splitting attacks, and should be blocked.
     *
     * @param redirect
     * @return
     */
    public static boolean validateRedirectUrl(String redirect) {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note:</b> On platforms that support symlinks, this function will fail canonicalization if directorypath
     * is a symlink. For example, on MacOS X, /etc is actually /private/etc. If you mean to use /etc, use its real
     * path (/private/etc), not the symlink (/etc).</p>
     */
    public boolean isValidDirectoryPath(String context, String input, File parent, boolean allowNull) {
        try {
            getValidDirectoryPath(context, input, parent, allowNull);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    /**
     * Returns a canonicalized and validated directory path as a String, provided that the input
     * maps to an existing directory that is an existing subdirectory (at any level) of the specified parent. Invalid input
     * will generate a descriptive ValidationException.
     *
     * @param context   A descriptive name of the parameter that you are validating (e.g., LoginPage_UsernameField). This value is used by any logging or error handling that is done with respect to the value passed in.
     * @param input     The actual input data to validate.
     * @param allowNull If allowNull is true then an input that is NULL or an empty string will be legal. If allowNull is false then NULL or an empty String will throw a ValidationException.
     * @return A valid directory path
     * @throws ValidationException
     */
    public String getValidDirectoryPath(String context, String input, File parent, boolean allowNull) throws ValidationException {
        try {
            if (isEmpty(input)) {
                if (allowNull) {
                    return null;
                }
                throw new ValidationException("Input directory path required: context=" + context + ", input=" + input);
            }

            File dir = new File(input);

            // check dir exists and parent exists and dir is inside parent
            if (!dir.exists()) {
                throw new ValidationException("Invalid directory, does not exist: context=" + context + ", input=" + input);
            }
            if (!dir.isDirectory()) {
                throw new ValidationException("Invalid directory, not a directory: context=" + context + ", input=" + input);
            }
            if (!parent.exists()) {
                throw new ValidationException("Invalid directory, specified parent does not exist: context=" + context + ", input=" + input + ", parent=" + parent);
            }
            if (!parent.isDirectory()) {
                throw new ValidationException("Invalid directory, specified parent is not a directory: context=" + context + ", input=" + input + ", parent=" + parent);
            }
            if (!dir.getCanonicalPath().startsWith(parent.getCanonicalPath())) {
                throw new ValidationException("Invalid directory, not inside specified parent: context=" + context + ", input=" + input + ", parent=" + parent);
            }

            // check canonical form matches input
            String canonicalPath = dir.getCanonicalPath();
            String canonical = this.getValidInput(context, canonicalPath, "DirectoryName", 255, false);
            if (!canonical.equals(input)) {
                throw new ValidationException("Invalid directory name: Invalid directory name does not match the canonical path: context=" + context + ", input=" + input + ", canonical=" + canonical);
            }
            return canonical;
        } catch (Exception e) {
            throw new ValidationException("Invalid directory name: Failure to validate directory path: context=" + context + ", input=" + input, e);
        }
    }

    /**
     * Helper function to check if a String is empty
     *
     * @param input string input value
     * @return boolean response if input is empty or not
     */
    private final boolean isEmpty(String input) {
        return (input == null || input.trim().length() == 0);
    }

    /**
     * Validates data received from the browser and returns a safe version. Only
     * URL encoding is supported. Double encoding is treated as an attack.
     *
     * @param context      A descriptive name for the field to validate. This is used for error facing validation messages and element identification.
     * @param input        The actual user input data to validate.
     * @param type         The regular expression name which maps to the actual regular expression in the ESAPI validation configuration file
     * @param maxLength    The maximum String length allowed. If input is canonicalized per the canonicalize argument, then maxLength must be verified after canonicalization
     * @param allowNull    If allowNull is true then a input that is NULL or an empty string will be legal. If allowNull is false then NULL or an empty String will throw a ValidationException.
     * @param canonicalize If canonicalize is true then input will be canonicalized before validation
     * @return The user input, may be canonicalized if canonicalize argument is true
     * @throws ValidationException
     * @throws IntrusionException
     */
    public String getValidInput(String context, String input, String type, int maxLength, boolean allowNull) throws ValidationException {
        StringValidationRule rvr = new StringValidationRule(type, encoder);
        Pattern p = ESAPI.securityConfiguration().getValidationPattern(type);
        if (p != null) {
            rvr.addWhitelistPattern(p);
        } else {
            // Issue 232 - Specify requested type in exception message - CS
            throw new IllegalArgumentException("The selected type [" + type + "] was not set via the ESAPI validation configuration");
        }
        rvr.setMaximumLength(maxLength);
        rvr.setAllowNull(allowNull);
        rvr.setCanonicalize(true);
        return rvr.getValid(context, input);
    }

}