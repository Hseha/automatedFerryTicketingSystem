package com.mycompany.automatedferryticketingsystem.util;

import org.mindrot.jbcrypt.BCrypt;
import java.util.Scanner;

public class HashGenerator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("   FERRY SYSTEM: ADMIN PASSWORD GENERATOR     ");
        System.out.println("==============================================");
        
        System.out.print("Enter the password you want to use: ");
        String plainPassword = scanner.nextLine();

        if (plainPassword.trim().isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
            return;
        }

        // --- THE CORE LOGIC ---
        // BCrypt.gensalt() creates a unique salt for this specific password.
        // BCrypt.hashpw() mixes the password with the salt to create the hash.
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        System.out.println("\n[SUCCESS] Password has been hashed securely.");
        System.out.println("\n--- COPY AND PASTE THIS HASH INTO YOUR DATABASE ---");
        System.out.println(hashedPassword);
        System.out.println("--------------------------------------------------\n");

        System.out.println("SQL COMMAND FOR YOUR TERMINAL:");
        System.out.println("INSERT INTO admins (username, password_hash, full_name)");
        System.out.println("VALUES ('admin', '" + hashedPassword + "', 'System Admin');");
        
        scanner.close();
    }
}