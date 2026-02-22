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

    @Autowired
    private MorphoAnalyzer morphoAnalyzer;

    // ================================================================
    // addRoot
    // ================================================================

    public boolean addRoot(String rootText) {
        log.debug("Tentative d'ajout: {}", rootText);

        // FIX #1 : validation null/vide avant tout
        if (rootText == null || rootText.isBlank()) {
            throw new IllegalArgumentException("La racine ne peut pas être vide.");
        }

        // FIX #2 : utiliser analyserRacine() comme source unique de vérité.
        // Ancien code : ValidationUtils.estRacineValide() était appelé EN PREMIER,
        // puis morphoAnalyzer.analyserRacine() EN SECOND.
        // Les deux pouvaient avoir des règles de validation différentes (ex : alef simple),
        // provoquant des racines refusées par l'une et acceptées par l'autre.
        // Nouveau code : un seul appel à analyserRacine(), qui encapsule TOUTES les règles.
        Root analysis = morphoAnalyzer.analyserRacine(rootText);
        if (!analysis.isValid()) {
            throw new IllegalArgumentException(analysis.getErrorMessage());
        }

        // Déjà existante ?
        if (arbreRacines.existe(rootText)) {
            log.warn("Racine déjà présente: {}", rootText);
            return false;
        }

        // Insertion dans l'AVL
        boolean added = arbreRacines.inserer(rootText);

        if (added) {
            // Stocker le type calculé dans le noeud (cache)
            NoeudAVL noeud = arbreRacines.rechercher(rootText);
            if (noeud != null) {
                noeud.setTypeMorphologique(analysis.getType());
                // FIX #3 : stocker également le flag contientHamza dans le noeud
                // pour éviter de recalculer lors des transformations
                noeud.setContientHamza(analysis.isContientHamza());
                log.info("✅ '{}' ajoutée (Type: {} — {})",
                    rootText, analysis.getType().getNomArabe(),
                    analysis.getType().getNomFrancais());
            }
        }

        return added;
    }

    // ================================================================
    // searchRoot
    // ================================================================

    public NoeudAVL searchRoot(String root) {
        // FIX #4 : ne pas appeler rechercher() avec null → NullPointerException dans AVL
        if (root == null || root.isBlank()) return null;
        log.debug("Recherche: {}", root);
        return arbreRacines.rechercher(root);
    }

    // ================================================================
    // getRootType
    // ================================================================

    public RootType getRootType(String rootText) {
        NoeudAVL noeud = searchRoot(rootText);
        if (noeud == null) return null;

        // Type absent du cache → le calculer et le mettre en cache
        if (noeud.getTypeMorphologique() == null) {
            Root analysis = morphoAnalyzer.analyserRacine(rootText);
            if (analysis.isValid()) {
                noeud.setTypeMorphologique(analysis.getType());
                noeud.setContientHamza(analysis.isContientHamza()); // FIX #3
                log.info("Type mis en cache pour: {}", rootText);
            }
        }

        return noeud.getTypeMorphologique();
    }

    // ================================================================
    // setRootType — FIX #5 : méthode manquante requise par GenerationService
    // Ancien code : GenerationService appelait rootService.setRootType()
    // mais la méthode n'existait pas → erreur de compilation.
    // ================================================================
    public void setRootType(String rootText, RootType type) {
        NoeudAVL noeud = searchRoot(rootText);
        if (noeud != null) {
            noeud.setTypeMorphologique(type);
            log.debug("Type '{}' mis en cache pour '{}'", type.getNomArabe(), rootText);
        }
    }

    // ================================================================
    // getRootExplanation
    // ================================================================

    public String getRootExplanation(String rootText) {
        // FIX #6 : ancien code créait un new Root(rootText) sans setContientHamza(),
        // donc l'explication n'affichait jamais le suffixe hamza.
        // Nouveau code : analyserRacine() retourne un Root complet avec tous les flags.
        if (rootText == null || rootText.isBlank()) return "Racine vide.";

        NoeudAVL noeud = searchRoot(rootText);
        if (noeud == null) return "Racine non trouvée.";

        Root analysis = morphoAnalyzer.analyserRacine(rootText);
        if (!analysis.isValid()) return analysis.getErrorMessage();

        return morphoAnalyzer.genererExplication(analysis);
    }

    // ================================================================
    // rootExists
    // ================================================================

    public boolean rootExists(String root) {
        // FIX #4 : protection null
        if (root == null || root.isBlank()) return false;
        return arbreRacines.existe(root);
    }

    // ================================================================
    // deleteRoot
    // ================================================================

    public boolean deleteRoot(String root) {
        if (root == null || root.isBlank()) return false;
        log.debug("Suppression: {}", root);
        boolean deleted = arbreRacines.supprimer(root);
        if (deleted) log.info("Racine supprimée: {}", root);
        return deleted;
    }

    // ================================================================
    // getRoots — avec pagination
    // ================================================================

    public List<String> getRoots(String search, int page, int limit) {
        // FIX #7 : page et limit non validés → IndexOutOfBoundsException possible.
        // Ancien code : start = (page-1)*limit pouvait être négatif si page=0.
        if (page < 1) page = 1;
        if (limit < 1) limit = 10;

        List<String> allRoots = arbreRacines.parcourirInfixe();

        if (search != null && !search.isEmpty()) {
            allRoots = allRoots.stream()
                .filter(r -> r.startsWith(search))
                .toList();
        }

        int start = (page - 1) * limit;
        if (start >= allRoots.size()) return new ArrayList<>();

        int end = Math.min(start + limit, allRoots.size());
        return allRoots.subList(start, end);
    }

    // ================================================================
    // getTotalRoots
    // ================================================================

    public int getTotalRoots(String search) {
        List<String> allRoots = arbreRacines.parcourirInfixe();
        if (search != null && !search.isEmpty()) {
            return (int) allRoots.stream()
                .filter(r -> r.startsWith(search))
                .count();
        }
        return allRoots.size();
    }

    // ================================================================
    // loadRootsFromFile
    // ================================================================

    public int loadRootsFromFile(MultipartFile file) throws IOException {
        // FIX #8 : fichier null ou vide non vérifié → NullPointerException.
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide ou absent.");
        }

        log.info("Chargement depuis: {}", file.getOriginalFilename());
        int count = 0, skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                // FIX #2 : validation via analyserRacine() uniquement,
                // pas de double-validation avec ValidationUtils
                try {
                    if (addRoot(line)) {
                        count++;
                    } else {
                        skipped++; // doublon
                        log.debug("Doublon ligne {}: {}", lineNum, line);
                    }
                } catch (IllegalArgumentException e) {
                    skipped++;
                    log.warn("Ligne {} ignorée — '{}' : {}", lineNum, line, e.getMessage());
                }
            }
        }

        log.info("✅ Chargement terminé : {} ajoutées, {} ignorées", count, skipped);
        return count;
    }

    // ================================================================
    // addDerivativeToRoot
    // ================================================================

    public boolean addDerivativeToRoot(String root, String derivative) {
        // FIX #9 : dérivé null/vide non vérifié → données corrompues dans le noeud.
        if (root == null || root.isBlank()) return false;
        if (derivative == null || derivative.isBlank()) {
            log.warn("⚠️ Dérivé vide ignoré pour la racine '{}'", root);
            return false;
        }

        NoeudAVL noeud = searchRoot(root);
        if (noeud == null) return false;

        noeud.ajouterDerive(derivative);
        noeud.incrementerFrequenceRacine();
        return true;
    }

    // ================================================================
    // getAllNodes / getRootCount
    // ================================================================

    public List<NoeudAVL> getAllNodes() {
        return arbreRacines.obtenirTousLesNoeuds();
    }

    public int getRootCount() {
        return arbreRacines.getNombreRacines();
    }
}