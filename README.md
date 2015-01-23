# Model2 - domain specific models in Java

The Model2 System helps to work with domain specific models in Java. No pre-processors or new language elements, just the the standard Java platform. Everything you know from Java still applies and you can leverage both your experience and toolkits.

This work is heavily inspired by [Qi4j](http://qi4j.org/).

## Example

Creating and configuring a repository:

```java
repo = EntityRepository.newConfiguration()
        .setStore( new RecordStoreAdapter( new LuceneRecordStore() ) )
        .setEntities( new Class[] {Employee.class} )
        .create();
```

The Employee entity class:
```java
@Description( "Everything we know about an employee" )
public abstract class Person extends Entity {

    /** Simple property of type String, value might be null. */
    @Nullable
    protected Property<String>      name;

    /** Firstname, must not be null, defaults to "Ulli" is not initialized. */
    @DefaultValue("Ulli")
    protected Property<String>      firstname;

    /** Another simple property of type Date. */
    @Nullable
    protected Property<Date>        birthday;
}
```

...
