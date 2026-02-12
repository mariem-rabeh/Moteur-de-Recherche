package com.morphology.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.morphology.model.ArbreAVL;
import com.morphology.model.NoeudAVL;
import com.morphology.util.ValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RootService {
    
    private final ArbreAVL arbreRacines = new ArbreAVL();
    
    /**
     * Ajouter une racine
     */
    public boolean addRoot(String root) {
        log.debug("Tentative d'ajout de la racine: {}", root);
        
        if (!ValidationUtils.estRacineValide(root)) {
            throw new IllegalArgumentException(
                "Racine invalide: " + root + ". Une racine doit contenir exactement 3 caractères arabes."
            );
        }
        
        boolean added = arbreRacines.inserer(root);
        if (added) {
            log.info("Racine ajoutée avec succès: {}", root);
        } else {
            log.warn("La racine existe déjà: {}", root);
        }
        return added;
    }
    
    /**
     * Rechercher une racine
     */
    public NoeudAVL searchRoot(String root) {
        log.debug("Recherche de la racine: {}", root);
        return arbreRacines.rechercher(root);
    }
    
    /**
     * Vérifier si une racine existe
     */
    public boolean rootExists(String root) {
        return arbreRacines.existe(root);
    }
    
    /**
     * Supprimer une racine
     */
    public boolean deleteRoot(String root) {
        log.debug("Suppression de la racine: {}", root);
        boolean deleted = arbreRacines.supprimer(root);
        if (deleted) {
            log.info("Racine supprimée: {}", root);
        }
        return deleted;
    }
    
    /**
     * Obtenir toutes les racines (paginées)
     */
    public List<String> getRoots(String search, int page, int limit) {
        List<String> allRoots = arbreRacines.parcourirInfixe();
        
        // Filtrer par recherche si nécessaire
        if (search != null && !search.isEmpty()) {
            allRoots = allRoots.stream()
                .filter(root -> root.startsWith(search))
                .toList();
        }
        
        // Pagination
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, allRoots.size());
        
        if (start >= allRoots.size()) {
            return new ArrayList<>();
        }
        
        return allRoots.subList(start, end);
    }
    
    /**
     * Compter le nombre total de racines
     */
    public int getTotalRoots(String search) {
        List<String> allRoots = arbreRacines.parcourirInfixe();
        
        if (search != null && !search.isEmpty()) {
            return (int) allRoots.stream()
                .filter(root -> root.startsWith(search))
                .count();
        }
        
        return allRoots.size();
    }
    
    /**
     * Charger les racines depuis un fichier uploadé
     */
    public int loadRootsFromFile(MultipartFile file) throws IOException {
        log.info("Chargement des racines depuis le fichier: {}", file.getOriginalFilename());
        
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#") && ValidationUtils.estRacineValide(line)) {
                    if (arbreRacines.inserer(line)) {
                        count++;
                    }
                }
            }
        }
        
        log.info("{} racines chargées avec succès", count);
        return count;
    }
    
    /**
     * Ajouter un dérivé à une racine
     */
    public boolean addDerivativeToRoot(String root, String derivative) {
        NoeudAVL noeud = searchRoot(root);
        if (noeud != null) {
            noeud.ajouterDerive(derivative);
            noeud.incrementerFrequenceRacine();
            return true;
        }
        return false;
    }
    
    /**
     * Obtenir tous les noeuds
     */
    public List<NoeudAVL> getAllNodes() {
        return arbreRacines.obtenirTousLesNoeuds();
    }
    
    /**
     * Obtenir le nombre de racines
     */
    public int getRootCount() {
        return arbreRacines.getNombreRacines();
    }
}