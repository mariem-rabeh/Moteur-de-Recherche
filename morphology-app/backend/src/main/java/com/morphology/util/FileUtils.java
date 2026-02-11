package com.morphology.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaires pour la gestion des fichiers avec support UTF-8
 */
public class FileUtils {

    /**
     * Lire un fichier ligne par ligne
     */
    public static List<String> lireFichier(String cheminFichier) throws IOException {
        List<String> lignes = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(cheminFichier), 
                    StandardCharsets.UTF_8))) {
            
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                lignes.add(ligne);
            }
        }
        
        return lignes;
    }

    /**
     * Écrire des lignes dans un fichier
     */
    public static void ecrireFichier(String cheminFichier, List<String> lignes) 
            throws IOException {
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(cheminFichier), 
                    StandardCharsets.UTF_8))) {
            
            for (String ligne : lignes) {
                writer.write(ligne);
                writer.newLine();
            }
        }
    }

    /**
     * Ajouter des lignes à la fin d'un fichier
     */
    public static void ajouterAFichier(String cheminFichier, List<String> lignes) 
            throws IOException {
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(cheminFichier, true), 
                    StandardCharsets.UTF_8))) {
            
            for (String ligne : lignes) {
                writer.write(ligne);
                writer.newLine();
            }
        }
    }

    /**
     * Vérifier si un fichier existe
     */
    public static boolean fichierExiste(String cheminFichier) {
        File fichier = new File(cheminFichier);
        return fichier.exists() && fichier.isFile();
    }

    /**
     * Créer un fichier s'il n'existe pas
     */
    public static void creerFichierSiInexistant(String cheminFichier) 
            throws IOException {
        
        File fichier = new File(cheminFichier);
        
        if (!fichier.exists()) {
            // Créer les répertoires parents si nécessaire
            File parent = fichier.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            fichier.createNewFile();
        }
    }

    /**
     * Compter le nombre de lignes dans un fichier
     */
    public static int compterLignes(String cheminFichier) throws IOException {
        int count = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(cheminFichier), 
                    StandardCharsets.UTF_8))) {
            
            while (reader.readLine() != null) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * Lire une ligne spécifique d'un fichier
     */
    public static String lireLigne(String cheminFichier, int numeroLigne) 
            throws IOException {
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(cheminFichier), 
                    StandardCharsets.UTF_8))) {
            
            String ligne;
            int compteur = 0;
            
            while ((ligne = reader.readLine()) != null) {
                if (compteur == numeroLigne) {
                    return ligne;
                }
                compteur++;
            }
        }
        
        return null;
    }

    /**
     * Supprimer un fichier
     */
    public static boolean supprimerFichier(String cheminFichier) {
        File fichier = new File(cheminFichier);
        return fichier.delete();
    }

    /**
     * Copier un fichier
     */
    public static void copierFichier(String source, String destination) 
            throws IOException {
        
        List<String> lignes = lireFichier(source);
        ecrireFichier(destination, lignes);
    }

    /**
     * Copier un fichier avec NIO (plus rapide pour gros fichiers)
     */
    public static void copierFichierNIO(String source, String destination) 
            throws IOException {
        
        Path sourcePath = Paths.get(source);
        Path destPath = Paths.get(destination);
        
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Obtenir l'extension d'un fichier
     */
    public static String obtenirExtension(String cheminFichier) {
        int index = cheminFichier.lastIndexOf('.');
        if (index > 0 && index < cheminFichier.length() - 1) {
            return cheminFichier.substring(index + 1);
        }
        return "";
    }

    /**
     * Obtenir le nom du fichier sans le chemin
     */
    public static String obtenirNomFichier(String cheminFichier) {
        File fichier = new File(cheminFichier);
        return fichier.getName();
    }

    /**
     * Vérifier si un fichier est vide
     */
    public static boolean estVide(String cheminFichier) throws IOException {
        return compterLignes(cheminFichier) == 0;
    }

    /**
     * Obtenir la taille d'un fichier en octets
     */
    public static long obtenirTaille(String cheminFichier) {
        File fichier = new File(cheminFichier);
        return fichier.length();
    }

    /**
     * Créer un répertoire s'il n'existe pas
     */
    public static void creerRepertoire(String cheminRepertoire) {
        File repertoire = new File(cheminRepertoire);
        if (!repertoire.exists()) {
            repertoire.mkdirs();
        }
    }

    /**
     * Lister tous les fichiers d'un répertoire
     */
    public static List<String> listerFichiers(String cheminRepertoire) {
        List<String> fichiers = new ArrayList<>();
        File repertoire = new File(cheminRepertoire);
        
        if (repertoire.exists() && repertoire.isDirectory()) {
            File[] listeFichiers = repertoire.listFiles();
            if (listeFichiers != null) {
                for (File fichier : listeFichiers) {
                    if (fichier.isFile()) {
                        fichiers.add(fichier.getName());
                    }
                }
            }
        }
        
        return fichiers;
    }

    /**
     * Sauvegarder un InputStream dans un fichier
     */
    public static void sauvegarderInputStream(InputStream inputStream, 
                                               String cheminDestination) 
            throws IOException {
        
        try (OutputStream outputStream = new FileOutputStream(cheminDestination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Lire tout le contenu d'un fichier en une seule String
     */
    public static String lireContenuComplet(String cheminFichier) 
            throws IOException {
        
        StringBuilder contenu = new StringBuilder();
        List<String> lignes = lireFichier(cheminFichier);
        
        for (String ligne : lignes) {
            contenu.append(ligne).append("\n");
        }
        
        return contenu.toString();
    }

    /**
     * Écrire une String complète dans un fichier
     */
    public static void ecrireContenuComplet(String cheminFichier, String contenu) 
            throws IOException {
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(cheminFichier), 
                    StandardCharsets.UTF_8))) {
            
            writer.write(contenu);
        }
    }

    /**
     * Vérifier si un chemin est un répertoire
     */
    public static boolean estRepertoire(String chemin) {
        File file = new File(chemin);
        return file.exists() && file.isDirectory();
    }

    /**
     * Obtenir le chemin absolu d'un fichier
     */
    public static String obtenirCheminAbsolu(String chemin) {
        File file = new File(chemin);
        return file.getAbsolutePath();
    }
}
