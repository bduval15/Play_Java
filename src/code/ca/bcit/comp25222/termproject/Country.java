package ca.bcit.comp25222.termproject;
/**
 *
 * @author Braeden Duval
 *
 */
public class Country
{
    private final String countryName;
    private final String capitalCityName;
    private String[] facts;

    Country(final String countryName,
            final String capitalCityName)
    {
        this.countryName = countryName;
        this.capitalCityName = capitalCityName;

        validateCountryName();
        validateCapitalCityName();
    }

    private void validateCountryName()
    {
        if(countryName == null || countryName.isEmpty())
        {
            throw new IllegalArgumentException("Country name cannot be null or empty");
        }
    }

    private void validateCapitalCityName()
    {
        if(capitalCityName == null || capitalCityName.isEmpty())
        {
            throw new IllegalArgumentException("Capital city name cannot be null or empty");
        }
    }

    public void setFacts(final String[] facts)
    {
        this.facts = facts;
    }

    public String[] getFacts()
    {
        return facts;
    }

    public String getCountryName()
    {
        return countryName;
    }

    public String getCapitalCityName()
    {
        return capitalCityName;
    }
}
