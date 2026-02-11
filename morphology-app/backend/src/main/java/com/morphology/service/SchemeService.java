package com.morphology.service;

import com.morphology.dto.response.SchemeResponse;
import com.morphology.model.Scheme;
import com.morphology.model.TableHachage;
import com.morphology.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SchemeService {
    
    private final TableHachage tableSchemes = new TableHachage();
    private final List<String> schemeNames = new ArrayList<>();
    
    /**
     * Ajouter un schème
     */
    public boolean addScheme(String name, String rule) {
        log.debug("Ajout du schème: {} avec règle: {}", name, rule);
        
        if (!ValidationUtils.estRegleValide(rule)) {
            throw new IllegalArgumentException("La règle doit contenir les positions 1, 2 et 3");
        }
        
        Scheme existing = tableSchemes.rechercher(name);
        if (existing != null) {
            log.warn("Le schème existe déjà: {}", name);
            return false;
        }
        
        Scheme scheme = new Scheme(name, rule);
        tableSchemes.inserer(name, scheme);
        schemeNames.add(name);
        
        log.info("Schème ajouté avec succès: {}", name);
        return true;
    }
    
    /**
     * Mettre à jour un schème
     */
    public boolean updateScheme(String name, String newRule) {
        log.debug("Mise à jour du schème: {}", name);
        
        if (!ValidationUtils.estRegleValide(newRule)) {
            throw new IllegalArgumentException("La règle doit contenir les positions 1, 2 et 3");
        }
        
        Scheme existing = tableSchemes.rechercher(name);
        if (existing == null) {
            log.warn("Schème non trouvé: {}", name);
            return false;
        }
        
        Scheme updated = new Scheme(name, newRule);
        tableSchemes.inserer(name, updated);
        
        log.info("Schème mis à jour: {}", name);
        return true;
    }
    
    /**
     * Supprimer un schème
     */
    public boolean deleteScheme(String name) {
        log.debug("Suppression du schème: {}", name);
        
        boolean deleted = tableSchemes.supprimer(name);
        if (deleted) {
            schemeNames.remove(name);
            log.info("Schème supprimé: {}", name);
        }
        return deleted;
    }
    
    /**
     * Rechercher un schème
     */
    public Scheme searchScheme(String name) {
        return tableSchemes.rechercher(name);
    }
    
    /**
     * Vérifier si un schème existe
     */
    public boolean schemeExists(String name) {
        return tableSchemes.rechercher(name) != null;
    }
    
    /**
     * Obtenir tous les schèmes
     */
    public List<SchemeResponse> getAllSchemes() {
        List<SchemeResponse> schemes = new ArrayList<>();
        for (String name : schemeNames) {
            Scheme scheme = tableSchemes.rechercher(name);
            if (scheme != null) {
                schemes.add(new SchemeResponse(scheme.getNom(), scheme.getRegle()));
            }
        }
        return schemes;
    }
    
    /**
     * Obtenir les noms de tous les schèmes
     */
    public List<String> getSchemeNames() {
        return new ArrayList<>(schemeNames);
    }
    
    /**
     * Charger les schèmes depuis un fichier uploadé
     */
    public int loadSchemesFromFile(MultipartFile file) throws IOException {
        log.info("Chargement des schèmes depuis le fichier: {}", file.getOriginalFilename());
        
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
                        String name = parts[0].trim();
                        String rule = parts[1].trim();
                        if (addScheme(name, rule)) {
                            count++;
                        }
                    }
                }
            }
        }
        
        log.info("{} schèmes chargés avec succès", count);
        return count;
    }
    
    /**
     * Appliquer un schème à une racine
     */
    public String applyScheme(String schemeName, String root) {
        Scheme scheme = searchScheme(schemeName);
        if (scheme == null) {
            return "Erreur : Schème '" + schemeName + "' non trouvé.";
        }
        
        if (root == null || root.length() != 3) {
            return "Erreur : La racine doit contenir exactement 3 lettres.";
        }
        
        return scheme.appliquer(root);
    }
    
    /**
     * Obtenir le nombre de schèmes
     */
    public int getSchemeCount() {
        return schemeNames.size();
    }
}