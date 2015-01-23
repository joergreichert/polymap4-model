# Model2 - domain specific models in Java

The Model2 System helps to work with domain specific models in Java. No pre-processors or new language elements, just the the standard Java platform. Everything you know from Java still applies and you can leverage both your experience and toolkits.

This work is heavily inspired by [Qi4j](http://qi4j.org/).

## Example

Create and configure a **repository**:

```java
repo = EntityRepository.newConfiguration()
        .setStore( new RecordStoreAdapter( new LuceneRecordStore() ) )
        .setEntities( new Class[] {Person.class} )
        .create();
```

The Person **entity** class:
```java
@Description( "Everything we know about an employee" )
public class Person extends Entity {

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

**Create an entity** of type Person in the repository:
```java
UnitOfWork uow = repo.newUnitOfWork();
Person person = uow.createEntity( Person.class, null, (Person prototyp) -> {
        prototyp.name.set( "Model2" );
        return prototyp;
});

