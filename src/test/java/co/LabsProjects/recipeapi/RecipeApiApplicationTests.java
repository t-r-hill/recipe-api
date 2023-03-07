package co.LabsProjects.recipeapi;

import co.LabsProjects.recipeapi.exception.InvalidArgumentException;
import co.LabsProjects.recipeapi.exception.NoSuchRecipeException;
import co.LabsProjects.recipeapi.model.*;
import co.LabsProjects.recipeapi.repo.RecipeRepo;
import co.LabsProjects.recipeapi.repo.UserRepo;
import co.LabsProjects.recipeapi.service.RecipeService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = RecipeApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecipeApiApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	RecipeRepo recipeRepo;

	@MockBean
	RecipeService recipeService;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	UserRepo userRepo;

	@Test
	@Order(1)
	@WithUserDetails("user1")
	public void testGetRecipeByIdSuccessBehavior() throws Exception {

		UserMeta user1Meta = UserMeta.builder().name("user1").email("user1@email.com").build();
		CustomUserDetails user1 = CustomUserDetails.builder()
				.username("user1")
				.password(encoder.encode("password1"))
				.userMeta(user1Meta)
				.authorities(Collections.singletonList(new Role(Role.Roles.ROLE_USER))).build();

		UserMeta user2Meta = UserMeta.builder().name("user2").email("user2@email.com").build();
		CustomUserDetails user2 = CustomUserDetails.builder()
				.username("user2")
				.password(encoder.encode("password2"))
				.userMeta(user2Meta)
				.authorities(Arrays.asList(
						new Role(Role.Roles.ROLE_USER),
						new Role(Role.Roles.ROLE_ADMIN))).build();

		Ingredient ingredient = Ingredient.builder().name("flour").state("dry").amount("2 cups").build();
		Step step1 = Step.builder().description("put flour in bowl").stepNumber(1).build();
		Step step2 = Step.builder().description("eat it?").stepNumber(2).build();

		Review review = Review.builder().description("tasted pretty bad").rating(2).user(user1).build();

		Recipe recipe1 = Recipe.builder()
				.name("test recipe")
				.difficultyRating(10)
				.minutesToMake(2)
				.ingredients(Set.of(ingredient))
				.steps(Set.of(step1, step2))
				.reviews(Set.of(review))
				.user(user2)
				.build();

		when(recipeService.getRecipeById(anyLong())).thenReturn(recipe1);


		//set up GET request
		mockMvc.perform(get("/recipes/" + 1))

				//print response
				.andDo(print())
				//expect status 200 OK
				.andExpect(status().isOk())
				//expect return Content-Type header as application/json
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

				//confirm returned JSON values
				.andExpect(jsonPath("id").value(recipe1.getId()))
				.andExpect(jsonPath("minutesToMake").value(recipe1.getMinutesToMake()))
				.andExpect(jsonPath("reviews", hasSize(recipe1.getReviews().size())))
				.andExpect(jsonPath("ingredients", hasSize(recipe1.getIngredients().size())))
				.andExpect(jsonPath("steps", hasSize(recipe1.getSteps().size())))
//				.andExpect(jsonPath("username").value(recipe1.getUsername()))
				;
	}

	@Test
	@Order(2)
	public void testGetRecipeByIdFailureBehavior() throws Exception {

		when(recipeService.getRecipeById(anyLong())).thenThrow(new NoSuchRecipeException("No recipe exists with that ID"));

		//set up guaranteed to fail in testing environment request
		mockMvc.perform(get("/recipes/" + 1))

				//print response
				.andDo(print())
				//expect status 404 NOT FOUND
				.andExpect(status().isNotFound())
				//confirm that HTTP body contains correct error message
				.andExpect(content().string(containsString("No recipe exists with that ID")));
	}

	@Test
	@Order(3)
	public void testGetAllRecipesSuccessBehavior() throws Exception {

		UserMeta user1Meta = UserMeta.builder().name("user1").email("user1@email.com").build();
		CustomUserDetails user1 = CustomUserDetails.builder()
				.username("user1")
				.password(encoder.encode("password1"))
				.userMeta(user1Meta)
				.authorities(Collections.singletonList(new Role(Role.Roles.ROLE_USER))).build();

		UserMeta user2Meta = UserMeta.builder().name("user2").email("user2@email.com").build();
		CustomUserDetails user2 = CustomUserDetails.builder()
				.username("user2")
				.password(encoder.encode("password2"))
				.userMeta(user2Meta)
				.authorities(Arrays.asList(
						new Role(Role.Roles.ROLE_USER),
						new Role(Role.Roles.ROLE_ADMIN))).build();

		Ingredient ingredient = Ingredient.builder().name("flour").state("dry").amount("2 cups").build();
		Step step1 = Step.builder().description("put flour in bowl").stepNumber(1).build();
		Step step2 = Step.builder().description("eat it?").stepNumber(2).build();

		Review review = Review.builder().description("tasted pretty bad").rating(2).user(user1).build();

		Recipe recipe1 = Recipe.builder()
				.name("test recipe")
				.difficultyRating(10)
				.minutesToMake(2)
				.ingredients(Set.of(ingredient))
				.steps(Set.of(step1, step2))
				.reviews(Set.of(review))
				.user(user2)
				.id(1)
				.build();

		ingredient.setId(null);
		Recipe recipe2 = Recipe.builder()
				.steps(Set.of(Step.builder().description("test").build()))
				.ingredients(Set.of(Ingredient.builder().name("test ing").amount("1").state("dry").build()))
				.name("another test recipe")
				.difficultyRating(10)
				.minutesToMake(2)
				.user(user1)
				.id(2)
				.build();

		Recipe recipe3 = Recipe.builder()
				.steps(Set.of(Step.builder().description("test 2").build()))
				.ingredients(Set.of(Ingredient.builder().name("test ing 2").amount("2").state("wet").build()))
				.name("another another test recipe")
				.difficultyRating(5)
				.minutesToMake(2)
				.user(user2)
				.id(3)
				.build();

		Recipe recipe4 = Recipe.builder()
				.name("chocolate and potato chips")
				.difficultyRating(10)
				.minutesToMake(1)
				.ingredients(Set.of(
						Ingredient.builder().name("potato chips").amount("1 bag").build(),
						Ingredient.builder().name("chocolate").amount("1 bar").build()))
				.steps(Set.of(
						Step.builder().stepNumber(1).description("eat both items together").build()))
				.reviews(Set.of(
						Review.builder().user(user2).rating(10).description("this stuff is so good").build()
				))
				.user(user1)
				.id(4)
				.build();

		when(recipeService.getAllRecipes()).thenReturn(Arrays.asList(recipe1, recipe2, recipe3, recipe4));

		//set up get request for all recipe endpoint
		mockMvc.perform(get("/recipes"))

				//expect status is 200 OK
				.andExpect(status().isOk())

				//expect it will be returned as JSON
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

				//expect there are 4 entries
				.andExpect(jsonPath("$", hasSize(4)))

				//expect the first entry to have ID 1
				.andExpect(jsonPath("$[0].id").value(1))

				//expect the first entry to have name test recipe
				.andExpect(jsonPath("$[0].name").value("test recipe"))

				//expect the second entry to have id 2
				.andExpect(jsonPath("$[1].id").value(2))

				//expect the second entry to have a minutesToMake value of 2
				.andExpect(jsonPath("$[1].minutesToMake").value(2))

				//expect the third entry to have id 3
				.andExpect(jsonPath("$[2].id").value(3))

				//expect the third entry to have difficulty rating
				.andExpect(jsonPath("$[2].difficultyRating").value(5));
	}

	@Test
	@Order(4)
	@WithUserDetails("user2")
	public void testCreateNewRecipeSuccessBehavior() throws Exception {

		UserMeta user1Meta = UserMeta.builder().name("user1").email("user1@email.com").build();
		CustomUserDetails user1 = CustomUserDetails.builder()
				.username("user1")
				.password(encoder.encode("password1"))
				.userMeta(user1Meta)
				.authorities(Collections.singletonList(new Role(Role.Roles.ROLE_USER))).build();

		UserMeta user2Meta = UserMeta.builder().name("user2").email("user2@email.com").build();
		CustomUserDetails user2 = CustomUserDetails.builder()
				.username("user2")
				.password(encoder.encode("password2"))
				.userMeta(user2Meta)
				.authorities(Arrays.asList(
						new Role(Role.Roles.ROLE_USER),
						new Role(Role.Roles.ROLE_ADMIN))).build();

		Ingredient ingredient = Ingredient.builder().name("brown sugar").state("dry").amount("1 cup").build();
		Step step1 = Step.builder().description("heat pan").stepNumber(1).build();
		Step step2 = Step.builder().description("add sugar").stepNumber(2).build();

		Review review = Review.builder().description("was just caramel").rating(3).user(user1).build();

		Recipe recipe = Recipe.builder()
				.name("caramel in a pan")
				.difficultyRating(10)
				.minutesToMake(2)
				.ingredients(Set.of(ingredient))
				.steps(Set.of(step1, step2))
				.reviews(Set.of(review))
				.user(user2)
				.locationURI(new URI("http://localhost/recipes/new"))
				.build();

		when(recipeService.createNewRecipe(ArgumentMatchers.any(Recipe.class))).thenReturn(recipe);

		MockHttpServletResponse response =
				mockMvc.perform(post("/recipes")
								//set request Content-Type header
								.contentType("application/json")
								//set HTTP body equal to JSON based on recipe object
								.content(TestUtil.convertObjectToJsonBytes(recipe))
						)

						//confirm HTTP response meta
						.andExpect(status().isCreated())
						.andExpect(content().contentType("application/json"))
						//confirm Location header with new location of object matches the correct URL structure
						.andExpect(header().string("Location", containsString("http://localhost/recipes/")))

						//confirm some recipe data
						.andExpect(jsonPath("id").isNotEmpty())
						.andExpect(jsonPath("name").value("caramel in a pan"))

						//confirm ingredient data
						.andExpect(jsonPath("ingredients", hasSize(1)))
						.andExpect(jsonPath("ingredients[0].name").value("brown sugar"))
						.andExpect(jsonPath("ingredients[0].amount").value("1 cup"))

						//confirm step data
						.andExpect(jsonPath("steps", hasSize(2)))
						.andExpect(jsonPath("steps[0]").isNotEmpty())
						.andExpect(jsonPath("steps[1]").isNotEmpty())

						//confirm review data
						.andExpect(jsonPath("reviews", hasSize(1)))
//						.andExpect(jsonPath("reviews[0].username").value("idk"))
						.andReturn().getResponse();
	}

	@Test
	@Order(5)
	@WithUserDetails("user2")
	public void testCreateNewRecipeFailureBehavior() throws Exception {

		Recipe recipe = new Recipe();

		when(recipeService.createNewRecipe(ArgumentMatchers.any(Recipe.class))).thenThrow(new InvalidArgumentException("I'm an exception message"));

		//force failure with empty User object
		mockMvc.perform(
						post("/recipes")
								//set body equal to empty recipe object
								.content(TestUtil.convertObjectToJsonBytes(recipe))
								//set Content-Type header
								.contentType("application/json")
				)
				//confirm status code 400 BAD REQUEST
				.andExpect(status().isBadRequest())
				//confirm the body only contains a String
				.andExpect(jsonPath("$").isString());
	}

	@Test
//make sure this test runs last
	@Order(11)
	public void testGetAllRecipesFailureBehavior() throws Exception {

		//delete all entries to force error
		when(recipeService.getAllRecipes()).thenThrow(new NoSuchRecipeException("There are no recipes yet :( feel free to add one though"));

		//perform GET all recipes
		mockMvc.perform(get("/recipes"))

				.andDo(print())

				//expect 404 NOT FOUND
				.andExpect(status().isNotFound())

				//expect error message defined in RecipeService class
				.andExpect(jsonPath("$").value("There are no recipes yet :( feel free to add one though"));
	}


	@Test
	@Order(6)
	public void testGetRecipesByNameSuccessBehavior() throws Exception {

		UserMeta user1Meta = UserMeta.builder().name("user1").email("user1@email.com").build();
		CustomUserDetails user1 = CustomUserDetails.builder()
				.username("user1")
				.password(encoder.encode("password1"))
				.userMeta(user1Meta)
				.authorities(Collections.singletonList(new Role(Role.Roles.ROLE_USER))).build();

		UserMeta user2Meta = UserMeta.builder().name("user2").email("user2@email.com").build();
		CustomUserDetails user2 = CustomUserDetails.builder()
				.username("user2")
				.password(encoder.encode("password2"))
				.userMeta(user1Meta)
				.authorities(Arrays.asList(
						new Role(Role.Roles.ROLE_USER),
						new Role(Role.Roles.ROLE_ADMIN))).build();

		Ingredient ingredient = Ingredient.builder().name("flour").state("dry").amount("2 cups").build();
		Step step1 = Step.builder().description("put flour in bowl").stepNumber(1).build();
		Step step2 = Step.builder().description("eat it?").stepNumber(2).build();

		Review review = Review.builder().description("tasted pretty bad").rating(2).user(user1).build();

		Recipe recipe1 = Recipe.builder()
				.name("test recipe")
				.difficultyRating(10)
				.minutesToMake(2)
				.ingredients(Set.of(ingredient))
				.steps(Set.of(step1, step2))
				.reviews(Set.of(review))
				.user(user2)
				.id(1)
				.build();

		ingredient.setId(null);
		Recipe recipe2 = Recipe.builder()
				.steps(Set.of(Step.builder().description("test").build()))
				.ingredients(Set.of(Ingredient.builder().name("test ing").amount("1").state("dry").build()))
				.name("another test recipe")
				.difficultyRating(10)
				.minutesToMake(2)
				.user(user1)
				.id(2)
				.build();

		Recipe recipe3 = Recipe.builder()
				.steps(Set.of(Step.builder().description("test 2").build()))
				.ingredients(Set.of(Ingredient.builder().name("test ing 2").amount("2").state("wet").build()))
				.name("another another test recipe")
				.difficultyRating(5)
				.minutesToMake(2)
				.user(user2)
				.id(3)
				.build();

		Recipe recipe4 = Recipe.builder()
				.name("chocolate and potato chips")
				.difficultyRating(10)
				.minutesToMake(1)
				.ingredients(Set.of(
						Ingredient.builder().name("potato chips").amount("1 bag").build(),
						Ingredient.builder().name("chocolate").amount("1 bar").build()))
				.steps(Set.of(
						Step.builder().stepNumber(1).description("eat both items together").build()))
				.reviews(Set.of(
						Review.builder().user(user2).rating(10).description("this stuff is so good").build()
				))
				.user(user1)
				.id(4)
				.build();

		when(recipeService.getRecipesByName(eq("recipe"))).thenReturn(Arrays.asList(recipe1, recipe2, recipe3));
		when(recipeService.getRecipesByName(eq("potato"))).thenReturn(Arrays.asList(recipe4));

		//set up get request to search for recipes with names including the word recipe
		MvcResult mvcResult = mockMvc.perform(get("/recipes/search/name/recipe"))
				//expect 200 OK
				.andExpect(status().isOk())
				//expect JSON in return
				.andExpect(content().contentType("application/json"))
				//return the MvcResult
				.andReturn();

		//pull json byte array from the result
		byte[] jsonByteArray = mvcResult.getResponse().getContentAsByteArray();
		//convert the json bytes to an array of Recipe objects
		Recipe[] returnedRecipes = TestUtil.convertJsonBytesToObject(jsonByteArray, Recipe[].class);

		//confirm 3 recipes were returned
		assertThat(returnedRecipes.length).isEqualTo(3);


		for(Recipe r: returnedRecipes) {
			//confirm none of the recipes are null
			assertThat(r).isNotNull();
			//confirm they all have IDs
			assertThat(r).isNotNull();
			//confirm they all contain recipe in the name
			assertThat(r.getName()).contains("recipe");
		}

		//set up get request to search for recipes with names containing potato
		byte[] jsonBytes = mockMvc.perform(get("/recipes/search/name/potato"))
				//expect 200 OK
				.andExpect(status().isOk())
				//expect json
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				//return response byte array
				.andReturn().getResponse().getContentAsByteArray();

		//get recipes as a java array
		returnedRecipes = TestUtil.convertJsonBytesToObject(jsonBytes, Recipe[].class);

		//confirm only one recipe was returned
		assertThat(returnedRecipes.length).isEqualTo(1);

		//make sure the recipe isn't null
		assertThat(returnedRecipes[0]).isNotNull();

		//expect that the name should contain potato
		assertThat(returnedRecipes[0].getName()).contains("potato");
	}

	@Test
	@Order(7)
	public void testGetRecipeByNameFailureBehavior() throws Exception {

		when(recipeService.getRecipesByName(anyString())).thenThrow(new NoSuchRecipeException("No recipes could be found with that name."));

		byte[] contentAsByteArray = mockMvc.perform(get("/recipes/search/name/should not exist"))
				//expect 404 NOT FOUND
				.andExpect(status().isNotFound())
				//expect only a String in the body
				.andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
				//retrieve content byte array
				.andReturn().getResponse().getContentAsByteArray();

		//convert JSON to String
		String message = new String(contentAsByteArray);

		//confirm error message is correct
		assertThat(message).isEqualTo("No recipes could be found with that name.");
	}

	@Test
	@Order(8)
	@WithUserDetails("user2")
	public void testDeleteRecipeByIdSuccessBehavior() throws Exception {

		UserMeta user1Meta = UserMeta.builder().name("user1").email("user1@email.com").build();
		CustomUserDetails user1 = CustomUserDetails.builder()
				.username("user1")
				.password(encoder.encode("password1"))
				.userMeta(user1Meta)
				.authorities(Collections.singletonList(new Role(Role.Roles.ROLE_USER))).build();

		UserMeta user2Meta = UserMeta.builder().name("user2").email("user2@email.com").build();
		CustomUserDetails user2 = CustomUserDetails.builder()
				.username("user2")
				.password(encoder.encode("password2"))
				.userMeta(user1Meta)
				.authorities(Arrays.asList(
						new Role(Role.Roles.ROLE_USER),
						new Role(Role.Roles.ROLE_ADMIN))).build();

		Recipe recipe3 = Recipe.builder()
				.steps(Set.of(Step.builder().description("test 2").build()))
				.ingredients(Set.of(Ingredient.builder().name("test ing 2").amount("2").state("wet").build()))
				.name("another another test recipe")
				.difficultyRating(5)
				.minutesToMake(2)
				.user(user1)
				.id(3)
				.build();

		when(recipeService.deleteRecipeById(anyLong())).thenReturn(recipe3);

		//set up delete request
		byte[] deleteResponseByteArr = mockMvc.perform(delete("/recipes/" + recipe3.getId()))
				//confirm 200 OK was returned
				.andExpect(status().isOk())
				//confirm a String was returned
				.andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
				.andReturn().getResponse().getContentAsByteArray();

		//pull delete message from byte[]
		String returnedDeleteConfirmationMessage = new String(deleteResponseByteArr);

		//confirm the message is as expected using the previously acquired Recipe object
		assertThat(returnedDeleteConfirmationMessage).isEqualTo("The recipe with ID "  + recipe3.getId() + " and name " + recipe3.getName() + " was deleted.");
	}

	@Test
	@Order(9)
	@WithUserDetails("user2")
	public void testDeleteRecipeByIdFailureBehavior() throws Exception {

		when(recipeService.deleteRecipeById(anyLong())).thenThrow(new NoSuchRecipeException("No recipe with ID -1 could be found. Could not delete."));
		//force error with invalid ID
		mockMvc.perform(delete("/recipes/-1"))
				//expect 400 BAD REQUEST
				.andExpect(status().isBadRequest())
				//expect plain text aka a String
				.andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
				//confirm correct error message
				.andExpect(content().string(is("No recipe with ID -1 could be found. Could not delete.")));
	}

	@Test
	void contextLoads() {
	}

}
