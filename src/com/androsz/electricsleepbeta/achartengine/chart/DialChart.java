/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androsz.electricsleepbeta.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

import com.androsz.electricsleepbeta.achartengine.model.CategorySeries;
import com.androsz.electricsleepbeta.achartengine.renderer.DefaultRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.DialRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.DialRenderer.Type;
import com.androsz.electricsleepbeta.achartengine.renderer.SimpleSeriesRenderer;
import com.androsz.electricsleepbeta.achartengine.util.MathHelper;

/**
 * The dial chart rendering class.
 */
public class DialChart extends AbstractChart {
	/** The radius of the needle. */
	private static final int NEEDLE_RADIUS = 10;
	/**
	 * 
	 */
	private static final long serialVersionUID = -7067026841373365639L;
	/** The legend shape width. */
	private static final int SHAPE_WIDTH = 10;
	/** The series dataset. */
	private final CategorySeries mDataset;
	/** The series renderer. */
	private final DialRenderer mRenderer;

	/**
	 * Builds a new pie chart instance.
	 * 
	 * @param dataset
	 *            the series dataset
	 * @param renderer
	 *            the dial renderer
	 */
	public DialChart(CategorySeries dataset, DialRenderer renderer) {
		mDataset = dataset;
		mRenderer = renderer;
	}

	/**
	 * The graphical representation of the dial chart.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param x
	 *            the top left x value of the view to draw to
	 * @param y
	 *            the top left y value of the view to draw to
	 * @param width
	 *            the width of the view to draw to
	 * @param height
	 *            the height of the view to draw to
	 * @param paint
	 *            the paint
	 */
	@Override
	public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
		paint.setAntiAlias(mRenderer.isAntialiasing());
		paint.setStyle(Style.FILL);
		paint.setTextSize(mRenderer.getLabelsTextSize());
		int legendSize = mRenderer.getLegendHeight();
		if (mRenderer.isShowLegend() && legendSize == 0) {
			legendSize = height / 5;
		}
		final int left = x + 15;
		final int top = y + 5;
		final int right = x + width - 5;
		final int bottom = y + height - legendSize;
		drawBackground(mRenderer, canvas, x, y, width, height, paint, false,
				DefaultRenderer.NO_COLOR);

		final int sLength = mDataset.getItemCount();
		double total = 0;
		final String[] titles = new String[sLength];
		for (int i = 0; i < sLength; i++) {
			total += mDataset.getValue(i);
			titles[i] = mDataset.getCategory(i);
		}
		final int mRadius = Math.min(Math.abs(right - left), Math.abs(bottom - top));
		final int radius = (int) (mRadius * 0.35);
		final int centerX = (left + right) / 2;
		final int centerY = (bottom + top) / 2;
		final float shortRadius = radius * 0.9f;
		final float longRadius = radius * 1.1f;
		double min = mRenderer.getMinValue();
		double max = mRenderer.getMaxValue();
		final double angleMin = mRenderer.getAngleMin();
		final double angleMax = mRenderer.getAngleMax();
		if (!mRenderer.isMinValueSet() || !mRenderer.isMaxValueSet()) {
			final int count = mRenderer.getSeriesRendererCount();
			for (int i = 0; i < count; i++) {
				final double value = mDataset.getValue(i);
				if (!mRenderer.isMinValueSet()) {
					min = Math.min(min, value);
				}
				if (!mRenderer.isMaxValueSet()) {
					max = Math.max(max, value);
				}
			}
		}
		if (min == max) {
			min = min * 0.5;
			max = max * 1.5;
		}

		paint.setColor(mRenderer.getLabelsColor());
		double minorTicks = mRenderer.getMinorTicksSpacing();
		double majorTicks = mRenderer.getMajorTicksSpacing();
		if (minorTicks == MathHelper.NULL_VALUE) {
			minorTicks = (max - min) / 30;
		}
		if (majorTicks == MathHelper.NULL_VALUE) {
			majorTicks = (max - min) / 10;
		}
		drawTicks(canvas, min, max, angleMin, angleMax, centerX, centerY, longRadius, radius,
				minorTicks, paint, false);
		drawTicks(canvas, min, max, angleMin, angleMax, centerX, centerY, longRadius, shortRadius,
				majorTicks, paint, true);

		final int count = mRenderer.getSeriesRendererCount();
		for (int i = 0; i < count; i++) {
			final double angle = getAngleForValue(mDataset.getValue(i), angleMin, angleMax, min,
					max);
			paint.setColor(mRenderer.getSeriesRendererAt(i).getColor());
			final boolean type = mRenderer.getVisualTypeForIndex(i) == Type.ARROW;
			drawNeedle(canvas, angle, centerX, centerY, shortRadius, type, paint);
		}

		drawLegend(canvas, mRenderer, titles, left, right, y, width, height, legendSize, paint);
	}

	/**
	 * The graphical representation of the legend shape.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param renderer
	 *            the series renderer
	 * @param x
	 *            the x value of the point the shape should be drawn at
	 * @param y
	 *            the y value of the point the shape should be drawn at
	 * @param paint
	 *            the paint to be used for drawing
	 */
	@Override
	public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y,
			Paint paint) {
		canvas.drawRect(x, y - SHAPE_WIDTH / 2, x + SHAPE_WIDTH, y + SHAPE_WIDTH / 2, paint);
	}

	/**
	 * Returns the angle for a specific chart value.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param angle
	 *            the needle angle value
	 * @param centerX
	 *            the center x value
	 * @param centerY
	 *            the center y value
	 * @param radius
	 *            the radius
	 * @param arrow
	 *            if a needle or an arrow to be painted
	 * @param paint
	 *            the paint settings
	 * @return the angle
	 */
	private void drawNeedle(Canvas canvas, double angle, int centerX, int centerY, double radius,
			boolean arrow, Paint paint) {
		final double diff = Math.toRadians(90);
		final int needleSinValue = (int) (NEEDLE_RADIUS * Math.sin(angle - diff));
		final int needleCosValue = (int) (NEEDLE_RADIUS * Math.cos(angle - diff));
		final int needleX = (int) (radius * Math.sin(angle));
		final int needleY = (int) (radius * Math.cos(angle));
		final int needleCenterX = centerX + needleX;
		final int needleCenterY = centerY + needleY;
		float[] points;
		if (arrow) {
			final int arrowBaseX = centerX + (int) (radius * 0.85 * Math.sin(angle));
			final int arrowBaseY = centerY + (int) (radius * 0.85 * Math.cos(angle));
			points = new float[] { arrowBaseX - needleSinValue, arrowBaseY - needleCosValue,
					needleCenterX, needleCenterY, arrowBaseX + needleSinValue,
					arrowBaseY + needleCosValue };
			final float width = paint.getStrokeWidth();
			paint.setStrokeWidth(5);
			canvas.drawLine(centerX, centerY, needleCenterX, needleCenterY, paint);
			paint.setStrokeWidth(width);
		} else {
			points = new float[] { centerX - needleSinValue, centerY - needleCosValue,
					needleCenterX, needleCenterY, centerX + needleSinValue,
					centerY + needleCosValue };
		}
		drawPath(canvas, points, paint, true);
	}

	/**
	 * Draws the chart tick lines.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param min
	 *            the minimum chart value
	 * @param max
	 *            the maximum chart value
	 * @param minAngle
	 *            the minimum chart angle value
	 * @param maxAngle
	 *            the maximum chart angle value
	 * @param centerX
	 *            the center x value
	 * @param centerY
	 *            the center y value
	 * @param longRadius
	 *            the long radius
	 * @param shortRadius
	 *            the short radius
	 * @param ticks
	 *            the tick spacing
	 * @param paint
	 *            the paint settings
	 * @param labels
	 *            paint the labels
	 * @return the angle
	 */
	private void drawTicks(Canvas canvas, double min, double max, double minAngle, double maxAngle,
			int centerX, int centerY, double longRadius, double shortRadius, double ticks,
			Paint paint, boolean labels) {
		for (double i = min; i <= max; i += ticks) {
			final double angle = getAngleForValue(i, minAngle, maxAngle, min, max);
			final double sinValue = Math.sin(angle);
			final double cosValue = Math.cos(angle);
			final int x1 = Math.round(centerX + (float) (shortRadius * sinValue));
			final int y1 = Math.round(centerY + (float) (shortRadius * cosValue));
			final int x2 = Math.round(centerX + (float) (longRadius * sinValue));
			final int y2 = Math.round(centerY + (float) (longRadius * cosValue));
			canvas.drawLine(x1, y1, x2, y2, paint);
			if (labels) {
				paint.setTextAlign(Align.LEFT);
				if (x1 <= x2) {
					paint.setTextAlign(Align.RIGHT);
				}
				String text = i + "";
				if (Math.round(i) == (long) i) {
					text = (long) i + "";
				}
				canvas.drawText(text, x1, y1, paint);
			}
		}
	}

	/**
	 * Returns the angle for a specific chart value.
	 * 
	 * @param value
	 *            the chart value
	 * @param minAngle
	 *            the minimum chart angle value
	 * @param maxAngle
	 *            the maximum chart angle value
	 * @param min
	 *            the minimum chart value
	 * @param max
	 *            the maximum chart value
	 * @return the angle
	 */
	private double getAngleForValue(double value, double minAngle, double maxAngle, double min,
			double max) {
		final double angleDiff = maxAngle - minAngle;
		final double diff = max - min;
		return Math.toRadians(minAngle + (value - min) * angleDiff / diff);
	}

	/**
	 * Returns the legend shape width.
	 * 
	 * @return the legend shape width
	 */
	@Override
	public int getLegendShapeWidth() {
		return SHAPE_WIDTH;
	}

}
