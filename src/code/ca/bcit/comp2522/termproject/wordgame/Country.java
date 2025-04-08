package ca.bcit.comp2522.termproject.wordgame;

/**
 * Represents a country with its name, capital city, and optional facts.
 * <p>
 * This immutable class encapsulates a country's essential details: its name and the name of its capital city.
 * Both these fields are validated during construction to ensure that they are not null or empty. Although the
 * country name and capital city are immutable once the object is created, additional details (facts about the country)
 * can be provided later via the {@link #setFacts(String[])} method.
 * </p>
 * <p>
 * The class is declared final to prevent subclassing, which helps maintain its intended behavior and ensures that
 * the validation logic for the country name and capital city remains consistent.
 * </p>
 *
 * @author Braeden Duval
 * @version 1.0
 */

final class Country
{
    private static final int EMPTY_FACTS = 0;

    private final   String      countryName;
    private final   String      capitalCityName;
    private         String[]    facts;

    /**
     * Constructs a {@code Country} instance using the specified country name and capital city name.
     * <p>
     * Both parameters are validated to ensure they are not {@code null} or empty.
     * If either validation fails, the constructor throws an {@link IllegalArgumentException}.
     * </p>
     *
     * @param countryName     the name of the country; must not be {@code null} or empty.
     * @param capitalCityName the name of the capital city; must not be {@code null} or empty.
     * @throws IllegalArgumentException if {@code countryName} or {@code capitalCityName} is {@code null} or empty.
     */
    Country(final String countryName,
            final String capitalCityName)
    {
        validateCountryName(countryName);
        validateCapitalCityName(capitalCityName);

        this.countryName        = countryName;
        this.capitalCityName    = capitalCityName;
    }

    /*
     * Validates that the provided country name is neither null nor empty.
     *
     * This method ensures the integrity of the country name before assignment. If the provided
     * countryName fails the validation, an IllegalArgumentException is thrown.
     *
     *
     * @param countryName the country name to validate.
     * @throws IllegalArgumentException if countryName is null or an empty string.
     */
    private void validateCountryName(final String countryName)
    {
        if (countryName == null || countryName.isEmpty())
        {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
    }

    /*
     * Validates that the provided capital city name is neither null nor empty.
     *
     * This method verifies that the capital city name meets the necessary criteria before the object is created.
     * If the value is null or empty, the method throws an IllegalArgumentException.
     *
     *
     * @param capitalCityName the capital city name to validate.
     * @throws IllegalArgumentException if capitalCityName is null or empty.
     */
    private void validateCapitalCityName(final String capitalCityName)
    {
        if (capitalCityName == null || capitalCityName.isEmpty())
        {
            throw new IllegalArgumentException("Capital city name cannot be null or empty");
        }
    }

    /*
     * Validates the provided facts array.
     *
     * This method checks that:
     *
     *   The facts array is not null and has at least one element.
     *   Each string in the facts array is not null and is not empty (after trimming whitespace).
     *
     * If any of these conditions are not met, an IllegalArgumentException is thrown.
     *
     * @param facts the array of facts about the country.
     * @throws IllegalArgumentException if the facts array is null, empty, or if any fact is null or empty.
     */
    private void validateFacts(final String[] facts)
    {
        if (facts == null || facts.length == EMPTY_FACTS)
        {
            throw new IllegalArgumentException("Facts array cannot be null or empty");
        }
        for (final String fact : facts)
        {
            if (fact == null || fact.trim().isEmpty())
            {
                throw new IllegalArgumentException("Each fact must be non-null and non-empty");
            }
        }
    }

    /**
     * Sets additional facts about the country.
     * <p>
     * This method allows updating or adding facts about the country after the object has been constructed.
     * The provided array of facts is validated to ensure that it is not {@code null}, not empty, and that each
     * individual fact is neither {@code null} nor empty (ignoring surrounding whitespace). If the validation fails,
     * an {@link IllegalArgumentException} is thrown.
     * </p>
     *
     * @param facts an array of strings representing facts about the country.
     * @throws IllegalArgumentException if the facts array is {@code null},
     * empty, or contains any {@code null} or empty strings.
     */
    void setFacts(final String[] facts)
    {
        validateFacts(facts);
        this.facts = facts;
    }

    /**
     * Retrieves the facts associated with the country.
     * <p>
     * This method returns the array of facts that have been previously set via {@link #setFacts(String[])}.
     * If no facts have been set, this method will return {@code null}.
     * </p>
     *
     * @return an array of strings containing facts about the country, or {@code null} if no facts have been set.
     */
    String[] getFacts()
    {
        return facts;
    }

    /**
     * Retrieves the name of the country.
     * <p>
     * This method returns the immutable country name that was specified when the object was constructed.
     * </p>
     *
     * @return the country name as a {@code String}.
     */
    String getCountryName()
    {
        return countryName;
    }

    /**
     * Retrieves the name of the capital city.
     * <p>
     * This method returns the immutable capital city name that was provided during object construction.
     * </p>
     *
     * @return the capital city name as a {@code String}.
     */
    String getCapitalCityName()
    {
        return capitalCityName;
    }
}
