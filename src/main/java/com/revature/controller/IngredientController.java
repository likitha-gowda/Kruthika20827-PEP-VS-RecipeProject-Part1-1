package com.revature.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;

import com.revature.model.Ingredient;
import com.revature.service.IngredientService;

public class IngredientController {

    @SuppressWarnings("unused")
    private IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    public void getIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ingredientService.findIngredient(id)
            .ifPresentOrElse(
                ingredient -> ctx.status(200).json(ingredient),
                () -> ctx.status(404)
            );
    }

    public void deleteIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ingredientService.deleteIngredient(id);
        ctx.status(204);
    }

    public void updateIngredient(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Ingredient ingredient = ctx.bodyAsClass(Ingredient.class);
        ingredient.setId(id);

        if (ingredientService.findIngredient(id).isPresent()) {
            ingredientService.saveIngredient(ingredient);
            ctx.status(204);
        } else {
            ctx.status(404);
        }
    }

    // FIX: must respond with 201 Created (not 200)
    public void createIngredient(Context ctx) {
        Ingredient ingredient = ctx.bodyAsClass(Ingredient.class);
        ingredient.setId(0); // ensure it's treated as new
        ingredientService.saveIngredient(ingredient);
        ctx.status(201).json(ingredient);
    }

    public void getIngredients(Context ctx) {
        String term = ctx.queryParam("term");

        Integer page = getParamAsClassOrElse(ctx, "page", Integer.class, null);
        Integer pageSize = getParamAsClassOrElse(ctx, "pageSize", Integer.class, null);
        String sortBy = ctx.queryParam("sortBy");
        String sortDirection = ctx.queryParam("sortDirection");

        if (page != null && pageSize != null) {
            ctx.json(
                ingredientService.searchIngredients(term, page, pageSize, sortBy, sortDirection)
            );
        } else {
            ctx.json(ingredientService.searchIngredients(term));
        }
    }

    private <T> T getParamAsClassOrElse(Context ctx, String queryParam, Class<T> clazz, T defaultValue) {
        if (ctx.queryParam(queryParam) != null) {
            return ctx.queryParamAsClass(queryParam, clazz).get();
        } else {
            return defaultValue;
        }
    }

    public void configureRoutes(Javalin app) {
        app.get("/ingredients", this::getIngredients);
        app.get("/ingredients/{id}", this::getIngredient);
        app.post("/ingredients", this::createIngredient);
        app.put("/ingredients/{id}", this::updateIngredient);
        app.delete("/ingredients/{id}", this::deleteIngredient);
    }
}
