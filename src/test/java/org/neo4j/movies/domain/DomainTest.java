package org.neo4j.movies.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.graph.neo4j.finder.FinderFactory;
import org.springframework.data.graph.neo4j.finder.NodeFinder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mh
 * @since 04.03.11
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/movies-test-context.xml"})
@Transactional
public class DomainTest {

    @Autowired
    FinderFactory finderFactory;
    protected NodeFinder<Movie> movieFinder;

    @Before
    public void setUp() throws Exception {
        movieFinder = finderFactory.createNodeEntityFinder(Movie.class);
    }

    @Test
    public void actorCanPlayARoleInAMovie() {
        Person tomHanks = new Person("1","Tom Hanks").persist();
        Movie forestGump = new Movie("1", "Forrest Gump").persist();

        Role role = tomHanks.playedIn(forestGump, "Forrest");

        Movie foundForestGump = this.movieFinder.findByPropertyValue("movies", "id", "1");

        assertEquals("created and looked up movie equal", forestGump, foundForestGump);
        Role firstRole = foundForestGump.getRoles().iterator().next();
        assertEquals("role forrest",role, firstRole);
        assertEquals("role forrest","Forrest", firstRole.getName());
    }

    @Test
    public void canFindMovieByTitleQuery() {
        Movie forestGump = new Movie("1", "Forrest Gump").persist();
        Iterator<Movie> queryResults = movieFinder.findAllByQuery("search", "title", "Forre*").iterator();
        assertTrue("found movie by query",queryResults.hasNext());
        Movie foundMovie = queryResults.next();
        assertEquals("created and looked up movie equal", forestGump, foundMovie);
        assertFalse("found only one movie by query", queryResults.hasNext());
    }

    @Test
    public void userCanRateMovie() {
        Movie movie= new Movie("1","Forrest Gump").persist();
        User user = new User("ich","Micha","password").persist();
        Rating awesome = user.rate(movie, 5, "Awesome");

        NodeFinder<User> userFinder = finderFactory.createNodeEntityFinder(User.class);

        User foundUser = userFinder.findByPropertyValue("users", "login", "ich");
        Rating rating = user.getRatings().iterator().next();
        assertEquals(awesome,rating);
        assertEquals("Awesome",rating.getComment());
        assertEquals(5,rating.getStars());
        assertEquals(5,movie.getStars(),0);
    }
}
