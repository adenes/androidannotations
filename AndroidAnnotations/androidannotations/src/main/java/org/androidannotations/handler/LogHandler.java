/**
 * Copyright (C) 2010-2013 eBusiness Information, Excilys Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.androidannotations.handler;

import com.sun.codemodel.*;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.Log;
import org.androidannotations.api.Logger;
import org.androidannotations.helper.AnnotationHelper;
import org.androidannotations.helper.ModelConstants;
import org.androidannotations.helper.TargetAnnotationHelper;
import org.androidannotations.holder.EApplicationHolder;
import org.androidannotations.holder.EComponentHolder;
import org.androidannotations.model.AnnotationElements;
import org.androidannotations.process.IsValid;
import org.androidannotations.rclass.IRClass;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import java.util.Collections;

import static com.sun.codemodel.JExpr.ref;
import static com.sun.codemodel.JExpr.refthis;

public class LogHandler extends BaseAnnotationHandler<EComponentHolder> {

	private final TargetAnnotationHelper annotationHelper;

	public LogHandler(ProcessingEnvironment processingEnvironment) {
		super(Log.class, processingEnvironment);
		annotationHelper = new TargetAnnotationHelper(processingEnvironment, getTarget());
	}

	@Override
	public void validate(Element element, AnnotationElements validatedElements, IsValid valid) {
		validatorHelper.allowedType(element, valid, element.asType(), Collections.singletonList(Logger.class.getName()));
		validatorHelper.isNotPrivate(element, valid);
	}

	@Override
	public void process(Element element, EComponentHolder holder) {
		JClass loggerClass = refClass(Logger.class);

		String tag = annotationHelper.extractAnnotationParameter(element, "tag");

		Log.Enabled enabled = annotationHelper.extractAnnotationParameter(element, "enabled");
		JExpression enabledExpr;
		switch (enabled) {
			case AUTO:
				enabledExpr = refClass(androidManifest.getApplicationPackage() + ".BuildConfig").staticRef("DEBUG");
				break;
			case TRUE:
				enabledExpr = JExpr.lit(true);
				break;
			case FALSE:
				enabledExpr = JExpr.lit(false);
				break;
			default:
				throw new IllegalStateException("Unhandled case: " + enabled);
		}

		JBlock block = holder.getInitBody();


		JInvocation assignLogger = loggerClass.staticInvoke("logger")
				.arg(ref("this"))
				.arg(tag)
				.arg(enabledExpr);

		String fieldName = element.getSimpleName().toString();
		JFieldRef loggerField = ref(fieldName);

		block.assign(loggerField, assignLogger);
	}
}
