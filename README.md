Switch Between Profiles

You can activate a profile by:

1.	Using application.properties: Set the default profile here if you want to override the local profile in production or other environments.
```
spring.profiles.active=dev
```

2. Setting Environment Variables: Set SPRING_PROFILES_ACTIVE as an environment variable to specify the profile.
```
export SPRING_PROFILES_ACTIVE=prod
```

3. Using Command Line Arguments: Specify the profile when starting the application.
```
java -jar target/GitServiceAPI.jar --spring.profiles.active=staging
```

Example Usage

	•	Local Development: Use application-local.properties for development configurations, so you can test locally without affecting the staging or production environments.
	•	Staging and Production: Use application-staging.properties and application-prod.properties for more controlled deployments.

This setup allows you to switch easily between different environments by changing the active profile, with each environment having its own configuration.