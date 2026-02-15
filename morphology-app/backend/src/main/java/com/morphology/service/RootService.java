package com.morphology.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.morphology.model.ArbreAVL;
import com.morphology.model.NoeudAVL;
import com.morphology.model.Root;
import com.morphology.model.RootType;
import com.morphology.util.ValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RootService {
    
    private final ArbreAVL arbreRacines = new ArbreAVL();
    
    // ✅ INJECTION du MorphoAnalyzer
    @Autowired
    private MorphoAnalyzer morphoAnalyzer;
    
    /**
     * Ajouter une racine AVEC analyse morphologique automatique
     * Complexité : O(log n) pour l'insertion + O(1) pour l'analyse
     */
    public boolean addRoot(String rootText) {
        log.debug("Tentative d'ajout de la racine: {}", rootText);
        
        // 1. VALIDATION basique
        if (!ValidationUtils.estRacineValide(rootText)) {
            throw new IllegalArgumentException(
                "Racine invalide: " + rootText + ". Une racine doit contenir exactement 3 caractères arabes."
            );
        }
        
        // 2. ANALYSE morphologique (une seule fois)
        Root analysis = morphoAnalyzer.analyserRacine(rootText);
        
        if (!analysis.isValid()) {
            throw new IllegalArgumentException(analysis.getErrorMessage());
        }
        
        // 3. VÉRIFICATION si déjà existante
        NoeudAVL existing = arbreRacines.rechercher(rootText);
        if (existing != null) {
            log.warn("La racine existe déjà: {}", rootText);
            return false;
        }
        
        // 4. INSERTION dans l'arbre avec le type pré-calculé
        boolean added = arbreRacines.inserer(rootText);
        
        if (added) {
            // 5. STOCKAGE du type dans le noeud (CACHE)
            NoeudAVL noeud = arbreRacines.rechercher(rootText);
            if (noeud != null) {
                noeud.setTypeMorphologique(analysis.getType());
                log.info("✅ Racine '{}' ajoutée (Type: {} - {})", 
                         rootText, 
                         analysis.getType().getNomArabe(),
                         analysis.getType().getNomFrancais());
            }
        }
        
        return added;
    }
    
    /**
     * Rechercher une racine
     * Complexité : O(log n)
     */
    public NoeudAVL searchRoot(String root) {
        log.debug("Recherche de la racine: {}", root);
        return arbreRacines.rechercher(root);
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE : Obtenir le type morphologique (lecture du cache)
     * Complexité : O(log n) pour la recherche + O(1) pour la lecture du type
     */
    public RootType getRootType(String rootText) {
        NoeudAVL noeud = searchRoot(rootText);
        
        if (noeud == null) {
            return null;
        }
        
        // Si le type n'a pas été calculé (racines anciennes), on le calcule
        if (noeud.getTypeMorphologique() == null) {
            Root analysis = morphoAnalyzer.analyserRacine(rootText);
            if (analysis.isValid()) {
                noeud.setTypeMorphologique(analysis.getType());
                log.info("Type morphologique calculé et mis en cache pour: {}", rootText);
            }
        }
        
        return noeud.getTypeMorphologique();
    }
    
    /**
     * ✅ NOUVELLE MÉTHODE : Obtenir l'explication pédagogique
     * Complexité : O(log n) + O(1)
     */
    public String getRootExplanation(String rootText) {
        RootType type = getRootType(rootText);
        if (type == null) {
            return "Racine non trouvée";
        }
        
        Root analysis = new Root(rootText);
        analysis.setType(type);
        
        return morphoAnalyzer.genererExplication(analysis);
    }
    
    /**
     * Vérifier si une racine existe
     * Complexité : O(log n)
     */
    public boolean rootExists(String root) {
        return arbreRacines.existe(root);
    }
    
    /**
     * Supprimer une racine
     * Complexité : O(log n)
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
     * AVEC analyse morphologique automatique
     */
    public int loadRootsFromFile(MultipartFile file) throws IOException {
        log.info("Chargement des racines depuis le fichier: {}", file.getOriginalFilename());
        
        int count = 0;
        int skipped = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Ignorer les lignes vides et commentaires
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Validation et insertion
                if (ValidationUtils.estRacineValide(line)) {
                    try {
                        if (addRoot(line)) {
                            count++;
                        } else {
                            skipped++;
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Racine ignorée (invalide): {} - {}", line, e.getMessage());
                        skipped++;
                    }
                } else {
                    log.warn("Racine ignorée (format invalide): {}", line);
                    skipped++;
                }
            }
        }
        
        log.info("✅ Chargement terminé : {} ajoutées, {} ignorées", count, skipped);
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