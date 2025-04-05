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
    private final   String      countryName;
    private final   String      capitalCityName;
    private         String[]    facts;

    /**
     * Constructs a Country object with the given name and capital city name.
     * Ensures that both parameters are not null or empty.
     *
     * @param countryName     The name of the country.
     * @param capitalCityName The name of the capital city.
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
     * Validates that the country name is not null or empty.
     *
     * @throws IllegalArgumentException if the country name is null or empty.
     */
    private void validateCountryName(final String countryName)
    {
        if (countryName == null || countryName.isEmpty())
        {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
    }

    /*
     * Validates that the capital city name is not null or empty.
     *
     * @throws IllegalArgumentException if the capital city name is null or empty.
     */
    private void validateCapitalCityName(final String capitalCityName)
    {
        if (capitalCityName == null || capitalCityName.isEmpty())
        {
            throw new IllegalArgumentException("Capital city name cannot be null or empty");
        }
    }

    /**
     * Sets the facts related to the country.
     *
     * @param facts An array of facts about the country.
     */
    void setFacts(final String[] facts)
    {
        this.facts = facts;
    }

    /**
     * Retrieves the facts about the country.
     *
     * @return An array of facts about the country.
     */
    String[] getFacts()
    {
        return facts;
    }

    /**
     * Retrieves the name of the country.
     *
     * @return The name of the country.
     */
    String getCountryName()
    {
        return countryName;
    }

    /**
     * Retrieves the name of the capital city of the country.
     *
     * @return The name of the capital city.
     */
    String getCapitalCityName()
    {
        return capitalCityName;
    }
}
