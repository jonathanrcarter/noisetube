class AddRegion < ActiveRecord::Migration
  def self.up
    add_column :cities, :region, :string
  end

  def self.down
    remove_column :cities, :region
  end
end
