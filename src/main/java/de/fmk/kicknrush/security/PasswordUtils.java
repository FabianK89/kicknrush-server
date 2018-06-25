package de.fmk.kicknrush.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;


/**
 * http://www.appsdeveloperblog.com/encrypt-user-password-example-java/
 */
public class PasswordUtils {
	private static final int    ITERATIONS = 10000;
	private static final int    KEY_LENGTH = 256;
	private static final Random RANDOM     = new SecureRandom();
	private static final String ALPHABET   = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


	public static String getSalt(int length) {
		final StringBuilder returnValue = new StringBuilder(length);

		for (int i = 0; i < length; i++)
			returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));

		return new String(returnValue);
	}


	public static byte[] hash(char[] password, byte[] salt) {
		final PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);

		Arrays.fill(password, Character.MIN_VALUE);

		try {
			final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

			return skf.generateSecret(spec).getEncoded();
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			throw new AssertionError("Error while hashing a password: " + ex.getMessage());
		}
		finally {
			spec.clearPassword();
		}
	}


	public static String generateSecurePassword(String password, String salt) {
		final byte[] securePassword;

		securePassword = hash(password.toCharArray(), salt.getBytes());

		return Base64.getEncoder().encodeToString(securePassword);
	}


	public static boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {
		final String newSecurePassword;

		newSecurePassword = generateSecurePassword(providedPassword, salt);

		return newSecurePassword.equalsIgnoreCase(securedPassword);
	}
}
