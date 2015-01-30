package exml.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import exml.objects.ObjectSchema;

@Retention(RetentionPolicy.RUNTIME)
public @interface MarkableSchema {
	public Class <? extends ObjectSchema> value();
}
