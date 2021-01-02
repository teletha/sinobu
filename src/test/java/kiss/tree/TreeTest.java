/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.tree;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.tree.HTML.ElementNode;

/**
 * @version 2017/02/06 14:00:16
 */
public class TreeTest {

    @Test
    public void node() {
        HTML html = new HTML() {
            {
                $("html");
            }
        };
        assert html.toString().equals("<html/>");
    }

    @Test
    public void nodeNest() {
        HTML html = new HTML() {
            {
                $("html", () -> {
                    $("body");
                });
            }
        };
        assert html.toString().equals("<html><body/></html>");
    }

    @Test
    public void nodes() {
        HTML html = new HTML() {
            {
                $("div");
                $("div");
            }
        };
        assert html.toString().equals("<div/><div/>");
    }

    @Test
    public void attribute() {
        HTML html = new HTML() {
            {
                $("div", attr("id", "test"));
            }
        };
        assert html.toString().equals("<div id='test'/>");
    }

    @Test
    public void attributeNullName() {
        HTML html = new HTML() {
            {
                $("div", attr(null, "ok"));
            }
        };
        assert html.toString().equals("<div/>");
    }

    @Test
    public void attributeEmptyName() {
        HTML html = new HTML() {
            {
                $("div", attr("", "ok"));
            }
        };
        assert html.toString().equals("<div/>");
    }

    @Test
    public void attributeNullValue() {
        HTML html = new HTML() {
            {
                $("input", attr("checked", null));
            }
        };
        assert html.toString().equals("<input checked/>");
    }

    @Test
    public void attributeEmptyValue() {
        HTML html = new HTML() {
            {
                $("div", attr("id", ""));
            }
        };
        assert html.toString().equals("<div id=''/>");
    }

    @Test
    public void attributeWithoutValue() {
        HTML html = new HTML() {
            {
                $("input", attr("checked"));
            }
        };
        assert html.toString().equals("<input checked/>");
    }

    @Test
    public void attributes2() {
        HTML html = new HTML() {
            {
                $("num", attr(1), attr(2), () -> {
                    $("ok");
                });
            }
        };
        assert html.toString().equals("<num 1 2><ok/></num>");
    }

    @Test
    public void attributes3() {
        HTML html = new HTML() {
            {
                $("num", attr(1), attr(2), attr(3), () -> {
                    $("ok");
                });
            }
        };
        assert html.toString().equals("<num 1 2 3><ok/></num>");
    }

    @Test
    public void attributes4() {
        HTML html = new HTML() {
            {
                $("num", attr(1), attr(2), attr(3), attr(4), () -> {
                    $("ok");
                });
            }
        };
        assert html.toString().equals("<num 1 2 3 4><ok/></num>");
    }

    @Test
    public void ifBoolean() {
        boolean ok = true;
        boolean fail = false;

        HTML html = new HTML() {
            {
                $("num", iｆ(ok, attr(1)), iｆ(fail, attr(2)));
            }
        };
        assert html.toString().equals("<num 1/>");
    }

    @Test
    public void ifSupplier() {
        Supplier<Boolean> ok = () -> true;
        Supplier<Boolean> fail = () -> false;
        Supplier<Boolean> nul = null;
        Supplier<Boolean> empty = () -> null;

        HTML html = new HTML() {
            {
                $("num", iｆ(ok, attr(1)), iｆ(fail, attr(2)), iｆ(nul, attr(3)), iｆ(empty, attr(4)));
            }
        };
        assert html.toString().equals("<num 1/>");
    }

    @Test
    public void either() {
        HTML html = new HTML() {
            {
                $("num", either(true, attr(1), attr(-1)), either(false, attr(2), attr(-2)));
            }
        };
        assert html.toString().equals("<num 1 -2/>");
    }

    @Test
    public void forRange() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(2, index -> {
                    $("li", () -> text(index));
                }));

            }
        };
        assert html.toString().equals("<ol><li>0</li><li>1</li></ol>");
    }

    @Test
    public void forRangeWithInitial() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(1, 3, index -> {
                    $("li", () -> text(index));
                }));

            }
        };
        assert html.toString().equals("<ol><li>1</li><li>2</li></ol>");
    }

    @Test
    public void forEnumType() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(ENUM.class, value -> {
                    $("li", () -> text(value.name()));
                }));

            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    /**
     * @version 2017/02/08 14:04:08
     */
    private static enum ENUM {
        A, B;
    }

    @Test
    public void forArray() {
        String[] array = {"A", "B"};

        HTML html = new HTML() {
            {
                $("ol", foŕ(array, value -> {
                    $("li", () -> text(value));
                }));

            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    @Test
    public void forIterableConsumer() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(list("A", "B"), item -> {
                    $("li", () -> text(item));
                }));

            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    @Test
    public void forIterableConsumerWithIndex() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(list("A", "B"), (id, item) -> {
                    $("li", () -> text(item + id));
                }));
            }
        };
        assert html.toString().equals("<ol><li>A0</li><li>B1</li></ol>");
    }

    @Test
    public void forIterableFunction() {
        HTML html = new HTML() {
            {
                $("div", foŕ(list("a", "b"), Id::new));
            }
        };
        assert html.toString().equals("<div id='a' id='b'/>");
    }

    @Test
    public void forIterableFunctionWithIndex() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(list("a", "b"), ListItem::new));
            }
        };
        assert html.toString().equals("<ol><li>a0</li><li>b1</li></ol>");
    }

    /**
     * @version 2017/02/08 16:44:25
     */
    static class ListItem implements Consumer<ElementNode> {

        private int index;

        private String name;

        /**
         * @param index
         * @param name
         */
        private ListItem(int index, String name) {
            this.index = index;
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(ElementNode parent) {
            parent.add(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return name + index;
        }
    }

    /**
     * <p>
     * Helper method to create name list.
     * </p>
     * 
     * @param names
     * @return
     */
    private <T> List<T> list(T... names) {
        return Arrays.asList(names);
    }

    @Test
    public void outerDefinedConsumer() {
        HTML html = new HTML() {
            {
                $("div", new Id("ok"));
            }
        };
        assert html.toString().equals("<div id='ok'/>");
    }

    /**
     * @version 2017/02/07 11:43:11
     */
    public static class Id implements Consumer<ElementNode> {

        public final String id;

        /**
         * @param id
         */
        private Id(String id) {
            this.id = id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(ElementNode parent) {
            parent.add(this);
        }
    }
}