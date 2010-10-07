/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrCallExpressionImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrCallExpressionTypeCalculator;

/**
 * @author Maxim.Medvedev
 */
public abstract class GrMethodCallImpl extends GrCallExpressionImpl implements GrMethodCall {
  private static final Function<GrMethodCall, PsiType> METHOD_CALL_TYPES_CALCULATOR = new Function<GrMethodCall, PsiType>() {
    @Nullable
    public PsiType fun(GrMethodCall callExpression) {
      for (GrCallExpressionTypeCalculator typeCalculator : GrCallExpressionTypeCalculator.EP_NAME.getExtensions()) {
        PsiType res = typeCalculator.calculateReturnType(callExpression);
        if (res != null) {
          return res;
        }
      }

      return null;
    }
  };

  public GrMethodCallImpl(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  public GroovyResolveResult[] getCallVariants(@Nullable GrExpression upToArgument) {
    final GrExpression invoked = getInvokedExpression();
    if (!(invoked instanceof GrReferenceExpressionImpl)) return GroovyResolveResult.EMPTY_ARRAY;

    return ((GrReferenceExpressionImpl)invoked).getCallVariants(upToArgument);
  }

  @Nullable
  public GrExpression getInvokedExpression() {
    return findChildByClass(GrExpression.class);
  }

  public PsiMethod resolveMethod() {
    final GrExpression methodExpr = getInvokedExpression();
    if (methodExpr instanceof GrReferenceExpression) {
      final PsiElement resolved = ((GrReferenceExpression) methodExpr).resolve();
      return resolved instanceof PsiMethod ? (PsiMethod) resolved : null;
    }

    return null;
  }

  public PsiType getType() {
    return GroovyPsiManager.getInstance(getProject()).getType(this, METHOD_CALL_TYPES_CALCULATOR);
  }

}
