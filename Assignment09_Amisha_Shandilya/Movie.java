import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

enum Category {
  ROMCOM,
  ROMANCE,
  COMEDY,
  ACTION,
  SCIENCE_FICTION,
  HORROR;
}

enum Language {
  HINDI,
  ENGLISH,
  MARATHI,
  TELGU,
  TAMIL,
  JAPANESE,
  KOREAN,
  THAI;
}

public class Movie implements java.io.Serializable {
  public int movieId;
  public String movieName;
  public Category movieType;
  public Language language;
  public Date releaseDate;
  public List<String> casting;
  public double rating;
  public double totalBusinessDone;

  public int getMovieId() {
    return movieId;
  }

  public void setMovieId(int movieId) {
    this.movieId = movieId;
  }

  public String getMovieName() {
    return movieName;
  }

  public void setMovieName(String movieName) {
    this.movieName = movieName;
  }

  public Category getMovieType() {
    return movieType;
  }

  public void setMovieType(Category movieType) {
    this.movieType = movieType;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  public List<String> getCasting() {
    return casting;
  }

  public void setCasting(List<String> casting) {
    this.casting = casting;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public double getTotalBusinessDone() {
    return totalBusinessDone;
  }

  public void setTotalBusinessDone(Double totalBusinessDone) {
    this.totalBusinessDone = totalBusinessDone;
  }

  public String toString() {
    return movieId + " " + movieName + " " + movieType + " " + language + " " + releaseDate + " "
        + casting + " " + rating + " " + totalBusinessDone;
  }

  public static List<Movie> populateMovies(String file) {
    List<Movie> list = new ArrayList<>();
    try {
      Scanner          scan   = new Scanner(new File(file));
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/YY");
      while (scan.hasNext()) {
        Movie movie             = new Movie();
        String[] line           = scan.nextLine().split(",");
        movie.movieId           = Integer.parseInt(line[0]);
        movie.movieName         = line[1];
        movie.movieType         = Category.valueOf(line[2].toUpperCase());
        movie.language          = Language.valueOf(line[3].toUpperCase());
        movie.releaseDate       = new Date(format.parse(line[4]).getTime());
        movie.casting           = List.of(line[5].split(";"));
        movie.rating            = Double.parseDouble(line[6]);
        movie.totalBusinessDone = Double.parseDouble(line[7]);
        list.add(movie);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    return list;
  }

  public static boolean addAllMoviesInDB(List<Movie> movies) {
    Connection conn = null;
    Statement  stmt = null;

    try {
      Class.forName("com.mysql.jdbc.Driver");
      conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviesdb", "root", "root123");
      stmt = conn.createStatement();
      for (Movie movie : movies) {
        String query = "INSERT INTO MOVIES VALUES (" + movie.movieId + "," + movie.movieName + ","
                       + movie.movieType + "," + movie.language + "," + movie.releaseDate + ","
                       + movie.casting + "," + movie.rating + "," + movie.totalBusinessDone + ")";
        stmt.executeUpdate(query);
      }
    } catch (Exception e) {
      System.out.println(e);
      return false;
    }
    return true;   // record added
  }

  public static void addMovie(Movie movie, List<Movie> movies) {
    movies.add(movie);
  }

  public static void serializeMovies(List<Movie> movies, String fileName) {
    try {
      FileOutputStream   fileOut = new FileOutputStream(fileName);
      ObjectOutputStream out     = new ObjectOutputStream(fileOut);
      out.writeObject(movies);   
      out.close();
      fileOut.close();
    } catch (IOException i) {
      i.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Movie> deserializeMovie(String fileName) {
    List<Movie> movies = new ArrayList<>();
    try {
      FileInputStream   fileIn = new FileInputStream(fileName);
      ObjectInputStream in     = new ObjectInputStream(fileIn);
      movies                   = (List<Movie>) in.readObject();
      in.close();
      fileIn.close();
    } catch (IOException i) {
      System.out.println(i);
    } catch (ClassNotFoundException c) {
      System.out.println("Movie class not found");
      c.printStackTrace();
    }
    return movies;
  }

  public static void updateBusiness(Movie movie, double amount, List<Movie> movies) {
    movie.totalBusinessDone = amount;
  }

  public static List<Movie> getMoviesRealeasedInYear(List<Movie> movies, int year) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy");
    return movies.stream()
        .filter((movie) -> df.format(movie.releaseDate) == String.valueOf(year))
        .collect(Collectors.toList());
  }

  public static void updateRatings(Movie movie, double rating, List<Movie> movies) {
    movie.rating = rating;
  }

  public static List<Movie> getMoviesByActor(List<Movie> movies, String... actorNames) {
    return movies.stream()
        .filter( (movie) -> {
          for(String actor: movie.casting)
            for(String actorName: actorNames)
              return true;
          return false;
        } )
        .collect(Collectors.toList());
  }
  
  public static Map<Language, Set<Movie> > businessDone(List<Movie> movies, double amount) {
    Map<Language, Set<Movie>> map = new HashMap<>();
    for(Language lan : Language.values())
      map.put(lan, new HashSet<Movie>());
    movies.forEach( (movie) -> {
      if(movie.totalBusinessDone > amount)
        map.get(movie.language).add(movie);
    });
    return map;
  }
}
