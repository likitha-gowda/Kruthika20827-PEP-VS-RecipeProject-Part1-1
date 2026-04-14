package com.revature.service;

import java.util.List;
import java.util.Optional;

import com.revature.dao.IngredientDAO;
import com.revature.model.Ingredient;
import com.revature.util.Page;
import com.revature.util.PageOptions;

public class IngredientService {

    private IngredientDAO ingredientDAO;

    public IngredientService(IngredientDAO ingredientDAO) {
        this.ingredientDAO = ingredientDAO;
    }

    //  find by id
    public Optional<Ingredient> findIngredient(int id) {
        return Optional.ofNullable(ingredientDAO.getIngredientById(id));
    }

    // pagination search (FIXED)
    public Page<Ingredient> searchIngredients(String term, int page, int pageSize, String sortBy, String sortDirection) {

        PageOptions options = new PageOptions(page, pageSize, sortBy, sortDirection);

        if (term == null) {
            return ingredientDAO.getAllIngredients(options);
        }

        return ingredientDAO.searchIngredients(term, options);
    }

    //  normal search
    public List<Ingredient> searchIngredients(String term) {
        if (term == null) return ingredientDAO.getAllIngredients();
        return ingredientDAO.searchIngredients(term);
    }

    // delete
    public void deleteIngredient(int id) {
        Ingredient ing = ingredientDAO.getIngredientById(id);
        if (ing != null) ingredientDAO.deleteIngredient(ing);
    }

    // save / update
    public void saveIngredient(Ingredient ingredient) {
        if (ingredient.getId() == 0) {
            int id = ingredientDAO.createIngredient(ingredient);
            ingredient.setId(id);
        } else {
            ingredientDAO.updateIngredient(ingredient);
        }
    }
}